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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.Fabric3JmsException;
import org.fabric3.binding.jms.runtime.helper.JmsHelper;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date$
 */
public class JmsTargetInterceptor implements Interceptor {

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    /**
     * Method name
     */
    private String methodName;

    /**
     * Request destination.
     */
    private Destination destination;

    /**
     * Request connection factory.
     */
    private ConnectionFactory connectionFactory;

    /**
     * Correlation scheme.
     */
    private CorrelationScheme correlationScheme;

    /**
     * Message receiver.
     */
    private Fabric3MessageReceiver messageReceiver;

    /**
     * Classloader to use.
     */
    private ClassLoader cl;

    /**
     * @param methodName Method name.
     * @param destination Request destination.
     * @param connectionFactory Request connection factory.
     * @param correlationScheme Correlation scheme.
     * @param messageReceiver Message receiver for response.
     */
    public JmsTargetInterceptor(String methodName,
                                Destination destination,
                                ConnectionFactory connectionFactory,
                                CorrelationScheme correlationScheme,
                                Fabric3MessageReceiver messageReceiver,
                                ClassLoader cl) {
        this.methodName = methodName;
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.correlationScheme = correlationScheme;
        this.messageReceiver = messageReceiver;
        this.cl = cl;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.invocation.Message)
     */
    public Message invoke(Message message) {

        Connection connection = null;
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            MessageProducer producer = session.createProducer(destination);
            Object[] payload = (Object[]) message.getBody();
//            payload = attachFramesToTail(payload, message.getWorkContext().getCallFrameStack());

            javax.jms.ObjectMessage jmsMessage = session.createObjectMessage(payload);
            jmsMessage.setObjectProperty("scaOperationName", methodName);

//            CallFrame previous = message.getWorkContext().peekCallFrame();
//            if( previous.getConversation()!=null){
//                Object conversationID = previous.getConversation().getConversationID();
//                if(conversationID != null){
//                    jmsMessage.setStringProperty("scaConversationId", String.valueOf(conversationID));
//                }
//                ConversationContext conversationContext = previous.getConversationContext();
//                if(ConversationContext.NEW.equals(conversationContext)){
//                    jmsMessage.setStringProperty("scaConversationStart", String.valueOf(conversationID));
//                }
//            }
            producer.send(jmsMessage);

            String correlationId = null;
            switch(correlationScheme) {
                case None:
                case RequestCorrelIDToCorrelID:
                    throw new UnsupportedOperationException("COrrelation scheme not supported");
                case RequestMsgIDToCorrelID:
                    correlationId = jmsMessage.getJMSMessageID();
            }
            session.commit();
            ObjectMessage responseMessage = (ObjectMessage) messageReceiver.receive(correlationId);
            Message response = new MessageImpl();
            response.setBody(responseMessage.getObject());
            return response;

        } catch(JMSException ex) {
            throw new Fabric3JmsException("Unable to receive response", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

	/**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

}
