/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.jms.runtime;

import java.io.Serializable;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.helper.MessageHelper;
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
 * @version $Revison$ $Date$
 */
public class RequestResponseMessageListener extends AbstractServiceMessageListener {
    public static final EncodeCallback CALLBACK = new ReturnEncodeCallback();

    public RequestResponseMessageListener(WireHolder wireHolder) {
        super(wireHolder);
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination)
            throws JmsServiceException, JmsBadMessageException, JMSException {
        String opName = request.getStringProperty(JmsConstants.OPERATION_HEADER);
        InvocationChainHolder holder = getInvocationChainHolder(opName);
        Interceptor interceptor = holder.getChain().getHeadInterceptor();
        PayloadType payloadType = holder.getPayloadType();

        Object payload = MessageHelper.getPayload(request, payloadType);
        switch (payloadType) {
        //
        case OBJECT:
            if (payload != null && !payload.getClass().isArray()) {
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
    }

    private void decodeAndInvoke(Message request,
                                 String opName,
                                 Interceptor interceptor,
                                 Object payload,
                                 PayloadType payloadType,
                                 MessageEncoder messageEncoder,
                                 Session responseSession,
                                 Destination responseDestination) throws JMSException, JmsServiceException, JmsBadMessageException {
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
            addCallFrame(inMessage, callbackUri);
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
            throw new JmsBadMessageException("Error decoding message", e);
        }
    }

    private void invoke(Message request,
                        Interceptor interceptor,
                        Object payload,
                        PayloadType payloadType,
                        Session responseSession,
                        Destination responseDestination) throws JmsServiceException, JMSException, JmsBadMessageException {
        WorkContext workContext = createWorkContext(request, wireHolder.getCallbackUri());
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
                              Message response) throws JMSException, JmsServiceException {
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
        if (outMessage.isFault()) {
            response.setBooleanProperty(JmsConstants.FAULT_HEADER, true);
        }
        MessageProducer producer = responseSession.createProducer(responseDestination);
        producer.send(response);
        TransactionType transactionType = wireHolder.getTransactionType();
        if (transactionType == TransactionType.LOCAL) {
            responseSession.commit();
        }
//        if (outMessage.isFault()) {
//            // report the service exception so it can be logged but do not roll back the transaction since the message was successfully processed
//            throw new JmsServiceException((Throwable) outMessage.getBody());
//        }
    }

    private Message createMessage(Object payload, Session session, PayloadType payloadType) throws JMSException {
        switch (payloadType) {
        case STREAM:
            throw new UnsupportedOperationException("Stream message not yet supported");
        case TEXT:
            if (payload != null && !(payload instanceof String)) {
                // this should not happen
                throw new IllegalArgumentException("Response payload is not a string: " + payload);
            }
            return session.createTextMessage((String) payload);
        case OBJECT:
            if (payload != null && !(payload instanceof Serializable)) {
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
