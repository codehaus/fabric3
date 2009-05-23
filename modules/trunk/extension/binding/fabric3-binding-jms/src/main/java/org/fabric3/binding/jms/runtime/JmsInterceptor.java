/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.binding.jms.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.binding.jms.runtime.helper.MessageHelper;
import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * Dispatches a service invocation to a JMS queue.
 *
 * @version $Revision$ $Date$
 */
public class JmsInterceptor implements Interceptor {
    private static final Message ONE_WAY_RESPONSE = new MessageImpl();
    private Interceptor next;
    private String methodName;
    private PayloadType payloadType;
    private Destination destination;
    private ConnectionFactory connectionFactory;
    private CorrelationScheme correlationScheme;
    private JmsResponseMessageListener messageReceiver;
    private MessageEncoder messageEncoder;
    private ParameterEncoder parameterEncoder;
    private ClassLoader cl;
    private boolean requestResponse;

    /**
     * Constructor.
     *
     * @param configuration the configuration template
     */
    public JmsInterceptor(InterceptorConfiguration configuration) {
        this.destination = configuration.getWireConfiguration().getRequestDestination();
        this.connectionFactory = configuration.getWireConfiguration().getRequestConnectionFactory();
        this.correlationScheme = configuration.getWireConfiguration().getCorrelationScheme();
        this.cl = configuration.getWireConfiguration().getClassloader();
        this.messageReceiver = configuration.getWireConfiguration().getMessageReceiver();
        // If message receiver is null, the interceptor is configured for one-way invocations. Otherwise, it is request-response.
        requestResponse = messageReceiver != null;
        this.methodName = configuration.getOperationName();
        this.payloadType = configuration.getPayloadType();
        this.messageEncoder = configuration.getMessageEncoder();
        this.parameterEncoder = configuration.getParameterEncoder();
    }

    public Message invoke(Message message) {


        Connection connection = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            MessageProducer producer = session.createProducer(destination);

            javax.jms.Message jmsMessage = createMessage(message, session);

            // enqueue the message
            producer.send(jmsMessage);

            String correlationId = null;
            switch (correlationScheme) {
            case None:
            case RequestCorrelIDToCorrelID:
                throw new UnsupportedOperationException("Correlation scheme not supported");
            case RequestMsgIDToCorrelID:
                correlationId = jmsMessage.getJMSMessageID();
            }
            session.commit();
            if (requestResponse) {
                // request-response, block on response
                return receive(correlationId);
            } else {
                return ONE_WAY_RESPONSE;
            }

        } catch (JMSException ex) {
            throw new ServiceRuntimeException("Unable to receive response", ex);
        } catch (IOException ex) {
            throw new ServiceRuntimeException("Error serializing callframe", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    private Message receive(String correlationId) throws JMSException {
        javax.jms.Message resultMessage = messageReceiver.receive(correlationId);
        Object responseMessage = MessageHelper.getPayload(resultMessage, payloadType);
        Message response = new MessageImpl();
        if (messageEncoder != null) {
            decode(response, responseMessage);
        } else {
            if (resultMessage.getBooleanProperty(JmsConstants.FAULT_HEADER)) {
                response.setBodyWithFault(responseMessage);
            } else {
                response.setBody(responseMessage);
            }
        }
        return response;
    }

    private void decode(Message response, Object responseMessage) {
        try {
            if (responseMessage == null) {
                throw new ServiceRuntimeException("Response type was null");
            } else if (String.class.equals(responseMessage.getClass())) {
                Message ret = messageEncoder.decodeResponse((String) responseMessage);
                if (ret.isFault()) {
                    Throwable deserialized = parameterEncoder.decodeFault(methodName, (String) ret.getBody());
                    response.setBodyWithFault(deserialized);
                } else {
                    Object deserialized = parameterEncoder.decodeResponse(methodName, (String) ret.getBody());
                    response.setBody(deserialized);
                }
            } else if (byte[].class.equals(responseMessage.getClass())) {
                Message ret = messageEncoder.decodeResponse((byte[]) responseMessage);
                if (ret.isFault()) {
                    Throwable deserialized = parameterEncoder.decodeFault(methodName, (byte[]) ret.getBody());
                    response.setBodyWithFault(deserialized);
                } else {
                    Object deserialized = parameterEncoder.decodeResponse(methodName, (byte[]) ret.getBody());
                    response.setBody(deserialized);
                }
            } else {
                throw new ServiceRuntimeException("Unnown response type: " + responseMessage.getClass().getName());
            }
        } catch (EncoderException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private javax.jms.Message createMessage(Message message, Session session) throws JMSException, IOException {
        Object[] payload = (Object[]) message.getBody();
        javax.jms.Message jmsMessage;
        switch (payloadType) {
        case OBJECT:
            jmsMessage = session.createObjectMessage(payload);
            setRoutingHeaders(message, jmsMessage);
            return jmsMessage;
        case STREAM:
            throw new UnsupportedOperationException("Not yet implemented");
        case TEXT:
            if (payload.length != 1) {
                throw new UnsupportedOperationException("Only single parameter operations are supported");
            }
            if (messageEncoder != null) {
                try {
                    String serialied = parameterEncoder.encodeText(message);
                    message.setBody(serialied);
                    TextMessage textMessage = session.createTextMessage();
                    EncodeCallback callback = new JMSEncodeCallback(textMessage);
                    String serializedMessage = messageEncoder.encodeText(methodName, message, callback);
                    textMessage.setText(serializedMessage);
                    return textMessage;
                } catch (EncoderException e) {
                    throw new ServiceRuntimeException(e);
                }
            } else {
                jmsMessage = session.createTextMessage((String) payload[0]);
                setRoutingHeaders(message, jmsMessage);
                return jmsMessage;
            }
        default:
            if (payload.length != 1) {
                throw new AssertionError("Bytes messages must have a single parameter");
            }
            jmsMessage = MessageHelper.createBytesMessage(session, payload[0], payloadType);
            setRoutingHeaders(message, jmsMessage);
            return jmsMessage;
        }
    }

    private void setRoutingHeaders(Message message, javax.jms.Message jmsMessage) throws JMSException, IOException {
        // add the operation name being invoked
        jmsMessage.setObjectProperty("scaOperationName", methodName);

        // serialize the callframes
        List<CallFrame> stack = message.getWorkContext().getCallFrameStack();
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        ObjectOutputStream stream = new ObjectOutputStream(bas);
        stream.writeObject(stack);
        stream.close();
        String encoded = Base64.encode(bas.toByteArray());
        jmsMessage.setStringProperty("f3Context", encoded);
    }

}
