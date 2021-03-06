/*
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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Message listener for service requests.
 *
 * @version $Revison$ $Date: 2008-03-18 05:24:49 +0800 (Tue, 18 Mar 2008) $
 */
public class ResponseMessageListenerImpl implements ResponseMessageListener {

    private Map<String, ChainHolder> operations;

    /**
     * Correlation scheme.
     */
    private final CorrelationScheme correlationScheme;

    /**
     * Transaction type.
     */
    private final TransactionType transactionType;

    /**
     * Callback URI.
     */
    private final String callBackURI;

    /**
     * @param chains            map of operations to interceptor chains.
     * @param correlationScheme correlation scheme.
     * @param messageTypes      the JMS message type used to enqueue service invocations keyed by operation name
     * @param transactionType   the type of transaction
     * @param callbackUri       the callback service uri
     */
    public ResponseMessageListenerImpl(Map<PhysicalOperationDefinition, InvocationChain> chains,
                                       CorrelationScheme correlationScheme,
                                       Map<String, PayloadType> messageTypes,
                                       TransactionType transactionType,
                                       String callbackUri) {
        this.operations = new HashMap<String, ChainHolder>();
        for (Entry<PhysicalOperationDefinition, InvocationChain> entry : chains.entrySet()) {
            String name = entry.getKey().getName();
            PayloadType type = messageTypes.get(name);
            if (type == null) {
                throw new IllegalArgumentException("No message type for operation: " + name);
            }
            this.operations.put(name, new ChainHolder(type, entry.getValue()));
        }
        this.correlationScheme = correlationScheme;
        this.transactionType = transactionType;
        this.callBackURI = callbackUri;
    }

    public void onMessage(Message request, Session responseSession, Destination responseDestination) {

        try {

            String opName = request.getStringProperty("scaOperationName");
            ChainHolder holder = getInterceptorHolder(opName);
            Interceptor interceptor = holder.getHeadInterceptor();
            PayloadType payloadType = holder.getType();
            Object payload = MessageHelper.getPayload(request, payloadType);
            if (payloadType != PayloadType.OBJECT && payloadType != PayloadType.TEXT) {
                // Encode primitives and streams as an array. Text payloads mus be decoded by an interceptor downstream. Object messages are already encoded.
                payload = new Object[]{payload};
            }
            WorkContext workContext = new WorkContext();
//            List<CallFrame> callFrames = (List<CallFrame>) payload[payload.length-1];
//
//            CallFrame previous = workContext.peekCallFrame();
            // Copy correlation and conversation information from incoming frame to new frame
            // Note that the callback URI is set to the callback address of this service so its callback wire can be mapped in the case of a
            // bidirectional service
//            Object id = previous.getCorrelationId(Object.class);
//            ConversationContext context = previous.getConversationContext();
//            Conversation conversation = previous.getConversation();
//            CallFrame frame = new CallFrame(callBackURI, id, conversation, context);
//            callFrames.add(frame);
//            workContext.addCallFrames(callFrames);
//            Object[] netPayload = new Object[payload.length-1];
//            System.arraycopy(payload, 0, netPayload, 0, payload.length-1);
            org.fabric3.spi.invocation.Message inMessage = new MessageImpl(payload, false, workContext);
            org.fabric3.spi.invocation.Message outMessage = interceptor.invoke(inMessage);

            Object responsePayload = outMessage.getBody();
            Message response = createMessage(responsePayload, responseSession, payloadType);

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
            
            if (transactionType == TransactionType.LOCAL){
                responseSession.commit();
            }

        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        }

    }

    /*
     * Finds the matching interceptor holder.
     */
    private ChainHolder getInterceptorHolder(String opName) {

        if (operations.size() == 1) {
            return operations.values().iterator().next();
        } else if (opName != null && operations.containsKey(opName)) {
            return operations.get(opName);
        } else if (operations.containsKey("onMessage")) {
            return operations.get("onMessage");
        } else {
            throw new Fabric3JmsException("Unable to match operation on the service contract");
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

    private class ChainHolder {
        private PayloadType type;
        private InvocationChain chain;

        private ChainHolder(PayloadType type, InvocationChain chain) {
            this.type = type;
            this.chain = chain;
        }

        public PayloadType getType() {
            return type;
        }

        public Interceptor getHeadInterceptor() {
            return chain.getHeadInterceptor();
        }
    }


}
