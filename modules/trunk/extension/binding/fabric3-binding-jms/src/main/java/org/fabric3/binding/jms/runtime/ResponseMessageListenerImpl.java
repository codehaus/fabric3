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
import java.util.Map;
import java.util.Map.Entry;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Message listeher for service requests.
 *
 * @version $Revison$ $Date: 2008-03-18 05:24:49 +0800 (Tue, 18 Mar 2008) $
 */
public class ResponseMessageListenerImpl implements ResponseMessageListener {


    /**
     * Operations available on the contract.
     */
    private final Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;

    /**
     * Correlation scheme.
     */
    private final CorrelationScheme correlationScheme;

    /**
     * Transaction type.
     */
    private final TransactionType transactionType;

    /***
     * Callback URI.
     */
    private final String callBackURI;

    /**
     * @param ops                Map of operation definitions.
     * @param correlationScheme  Correlation scheme.
     * @param transactionType    the type of transaction
     */
    public ResponseMessageListenerImpl(Map<String, Entry<PhysicalOperationDefinition, InvocationChain>> ops,
                                  CorrelationScheme correlationScheme,
                                  TransactionHandler transactionHandler,
                                  TransactionType transactionType,
                                  String callBackURI) {
        this.ops = ops;
        this.correlationScheme = correlationScheme;
        this.transactionType = transactionType;
        this.callBackURI = callBackURI;
    }

    /* (non-Javadoc)
     * @see org.fabric3.binding.jms.runtime.ResponseMessageListener#onMessage(javax.jms.Message, javax.jms.Session, javax.jms.Destination)
     */
    public void onMessage(Message request, Session responseSession, Destination responseDestination) {

        try {

            String opName = request.getStringProperty("scaOperationName");
            Interceptor interceptor = getInterceptor(opName);

            ObjectMessage objectMessage = (ObjectMessage) request;
            Object[] payload = (Object[])objectMessage.getObject();

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

            Message response = responseSession.createObjectMessage((Serializable) outMessage.getBody());

            switch (correlationScheme) {
                case RequestCorrelIDToCorrelID: {
                    response.setJMSCorrelationID(request.getJMSCorrelationID() );
                    break;
                }
                case RequestMsgIDToCorrelID: {
                    response.setJMSCorrelationID(request.getJMSMessageID());
                    break;
                }
            }
            MessageProducer producer = responseSession.createProducer(responseDestination);
            producer.send(response);

        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to send response", ex);
        }

    }

    /*
     * Finds the matching interceptor.
     */
    private Interceptor getInterceptor(String opName) {

        if (ops.size() == 1) {
            return ops.values().iterator().next().getValue().getHeadInterceptor();
        } else if (opName != null && ops.containsKey(opName)) {
            return ops.get(opName).getValue().getHeadInterceptor();
        } else if (ops.containsKey("onMessage")) {
            return ops.get("onMessage").getValue().getHeadInterceptor();
        } else {
            throw new Fabric3JmsException("Unable to match operation on the service contract");
        }

    }

}
