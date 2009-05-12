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

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.util.Base64;
import org.fabric3.spi.wire.Interceptor;

/**
 * Dispatches a service invocation to a JMS queue.
 *
 * @version $Revision$ $Date$
 */
public class JmsTargetInterceptor implements Interceptor {
    private Interceptor next;
    private String methodName;
    private PayloadType payloadType;
    private Destination destination;
    private ConnectionFactory connectionFactory;
    private CorrelationScheme correlationScheme;
    private JmsTargetMessageListener messageReceiver;
    private Serializer inputSerializer;
    private Serializer outputSerializer;
    private ClassLoader cl;

    /**
     * @param methodName        Method name.
     * @param payloadType       the type of JMS message to send
     * @param destination       Request destination.
     * @param connectionFactory Request connection factory.
     * @param correlationScheme Correlation scheme.
     * @param messageReceiver   Message receiver for response.
     * @param inputSerializer   The serializer or null of input payload transformation is not required
     * @param outputSerializer  The serializer or null of output payload transformation is not required
     * @param cl                the classloader for loading parameter types.
     */
    public JmsTargetInterceptor(String methodName,
                                PayloadType payloadType,
                                Destination destination,
                                ConnectionFactory connectionFactory,
                                CorrelationScheme correlationScheme,
                                JmsTargetMessageListener messageReceiver,
                                Serializer inputSerializer,
                                Serializer outputSerializer,
                                ClassLoader cl) {
        this.methodName = methodName;
        this.payloadType = payloadType;
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.correlationScheme = correlationScheme;
        this.messageReceiver = messageReceiver;
        this.inputSerializer = inputSerializer;
        this.outputSerializer = outputSerializer;
        this.cl = cl;
    }

    public Message invoke(Message message) {

        Message response = new MessageImpl();

        Connection connection = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            MessageProducer producer = session.createProducer(destination);
            Object[] payload = (Object[]) message.getBody();

            javax.jms.Message jmsMessage = createMessage(message, session, payload);

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
            if (messageReceiver != null) {
                javax.jms.Message resultMessage = messageReceiver.receive(correlationId);
                Object responseMessage = MessageHelper.getPayload(resultMessage, payloadType);
                if (inputSerializer != null) {
                    try {
                        Object deserialized = outputSerializer.deserializeResponse(Object.class, responseMessage);
                        response.setBody(deserialized);
                    } catch (SerializationException e) {
                        throw new ServiceRuntimeException(e);
                    }
                } else {
                    response.setBody(responseMessage);
                }
            }

            return response;

        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to receive response", ex);
        } catch (IOException ex) {
            throw new Fabric3JmsException("Error serializing callframe", ex);
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

    private javax.jms.Message createMessage(Message message, Session session, Object[] payload) throws JMSException, IOException {
        javax.jms.Message jmsMessage;
        switch (payloadType) {
        case OBJECT:
            jmsMessage = session.createObjectMessage(payload);
            break;
        case STREAM:
            throw new UnsupportedOperationException("Not yet implemented");
        case TEXT:
            if (payload.length != 1) {
                throw new UnsupportedOperationException("Only single parameter operations are supported");
            }
            if (inputSerializer != null) {
                try {
                    jmsMessage = session.createTextMessage(inputSerializer.serialize(String.class, payload[0]));
                } catch (SerializationException e) {
                    throw new ServiceRuntimeException(e);
                }
            } else {
                jmsMessage = session.createTextMessage((String) payload[0]);
            }
            break;
        default:
            if (payload.length != 1) {
                throw new AssertionError("Bytes messages must have a single parameter");
            }
            jmsMessage = MessageHelper.createBytesMessage(session, payload[0], payloadType);
        }
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

        return jmsMessage;
    }

}
