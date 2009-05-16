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

import java.io.Serializable;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.spi.binding.format.EncodeCallback;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;

/**
 * Listens for requests sent to a destination and dispatches them to a service, returning a response to the response destination.
 *
 * @version $Revison$ $Date: 2008-03-18 05:24:49 +0800 (Tue, 18 Mar 2008) $
 */
public class RequestResponseMessageListener extends AbstractSourceMessageListener {
    public static final EncodeCallback CALLBACK = new ReturnEncodeCallback();

    public RequestResponseMessageListener(WireHolder wireHolder) {
        super(wireHolder);
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination) throws JmsOperationException {
        try {
            String opName = request.getStringProperty("scaOperationName");
            InvocationChainHolder holder = getInvocationChainHolder(opName);
            Interceptor interceptor = holder.getChain().getHeadInterceptor();
            PayloadType payloadType = holder.getPayloadType();

            Object payload = MessageHelper.getPayload(request, payloadType);
            switch (payloadType) {
            //
            case OBJECT:
                if (payload == null || !payload.getClass().isArray()) {
                    payload = new Object[]{payload};
                }
                invoke(request, interceptor, payload, payloadType, responseSession, responseDestination);
                break;
            case TEXT:
                MessageEncoder messageEncoder = wireHolder.getMessageEncoder();
                if (messageEncoder != null) {
                    decodeAndInvoke(request, opName, interceptor, payload, payloadType, messageEncoder, responseSession, responseDestination);
                } else {
                    // non-encoded text
                    payload = new Object[]{payload};
                    invoke(request, interceptor, payload, payloadType, responseSession, responseDestination);
                }
                break;
            case STREAM:
                throw new UnsupportedOperationException();
            default:
                payload = new Object[]{payload};
                invoke(request, interceptor, payload, payloadType, responseSession, responseDestination);
                break;
            }

        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        }

    }

    private void decodeAndInvoke(Message request,
                                 String opName,
                                 Interceptor interceptor,
                                 Object payload,
                                 PayloadType payloadType,
                                 MessageEncoder messageEncoder,
                                 Session responseSession,
                                 Destination responseDestination) throws JMSException, JmsOperationException {
        try {
            JMSHeaderContext context = new JMSHeaderContext(request);
            org.fabric3.spi.invocation.Message inMessage = messageEncoder.decode((String) payload, context);
            ParameterEncoder parameterEncoder = wireHolder.getParameterEncoder();
            Object deserialized = parameterEncoder.decode(opName, (String) inMessage.getBody());
            if (deserialized == null) {
                inMessage.setBody(null);
            } else {
                inMessage.setBody(new Object[]{deserialized});
            }
            String callbackUri = wireHolder.getCallbackUri();
            JmsHelper.addCallFrame(inMessage, callbackUri);
            org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);
            String serialized = parameterEncoder.encodeText(outMessage);
            if (outMessage.isFault()) {
                outMessage.setBodyWithFault(serialized);
            } else {
                outMessage.setBody(serialized);
            }

            String serializedMessage = messageEncoder.encodeText(opName, outMessage, CALLBACK);

            Message response = createMessage(serializedMessage, responseSession, payloadType);
            sendResponse(request, responseSession, responseDestination, outMessage, response);
        } catch (EncoderException e) {
            throw new JmsOperationException(e);
        }
    }

    private void invoke(Message request,
                        Interceptor interceptor,
                        Object payload,
                        PayloadType payloadType,
                        Session responseSession,
                        Destination responseDestination) throws JmsOperationException, JMSException {
        WorkContext workContext = JmsHelper.createWorkContext(request, wireHolder.getCallbackUri());
        org.fabric3.spi.invocation.Message inMessage = new MessageImpl(payload, false, workContext);
        org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);

        Object responsePayload = outMessage.getBody();
        Message response = createMessage(responsePayload, responseSession, payloadType);
        sendResponse(request, responseSession, responseDestination, outMessage, response);
    }

    private void sendResponse(Message request,
                              Session responseSession,
                              Destination responseDestination,
                              org.fabric3.spi.invocation.Message outMessage,
                              Message response) throws JMSException, JmsOperationException {
        CorrelationScheme correlationScheme = wireHolder.getCorrelationScheme();
        switch (correlationScheme) {
        case RequestCorrelIDToCorrelID: {
            response.setJMSCorrelationID(request.getJMSCorrelationID());
            break;
        }
        case RequestMsgIDToCorrelID: {
            response.setJMSCorrelationID(request.getJMSMessageID());
            break;
        }
        }
        MessageProducer producer = responseSession.createProducer(responseDestination);
        producer.send(response);
        TransactionType transactionType = wireHolder.getTransactionType();
        if (transactionType == TransactionType.LOCAL) {
            responseSession.commit();
        }
        if (outMessage.isFault()) {
            // throw the original exception
            throw new JmsOperationException((Throwable) outMessage.getBody());
        }
    }

    private Message createMessage(Object payload, Session session, PayloadType payloadType) throws JMSException {
        switch (payloadType) {
        case STREAM:
            throw new UnsupportedOperationException("Stream message not yet supported");
        case TEXT:
            if (!(payload instanceof String)) {
                // this should not happen
                throw new IllegalArgumentException("Response payload is not a string: " + payload);
            }
            return session.createTextMessage((String) payload);
        case OBJECT:
            if (!(payload instanceof Serializable)) {
                // this should not happen
                throw new IllegalArgumentException("Response payload is not serializable: " + payload);
            }
            return session.createObjectMessage((Serializable) payload);
        default:
            return MessageHelper.createBytesMessage(session, payload, payloadType);
        }
    }

    private static class ReturnEncodeCallback implements EncodeCallback {

        public void encodeContentLengthHeader(long length) {
            // no op
        }

        public void encodeOperationHeader(String name) {
            // no op
        }

        public void encodeRoutingHeader(String header) {
            // no op
        }

        public void encodeRoutingHeader(byte[] header) {
            // no op
        }
    }
}
