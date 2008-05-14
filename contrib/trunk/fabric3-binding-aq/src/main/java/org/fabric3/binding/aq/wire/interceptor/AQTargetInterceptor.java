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
package org.fabric3.binding.aq.wire.interceptor;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnectionFactory;
import javax.jms.XASession;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.transport.Fabric3MessageReceiver;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision$ $Date: 2007-09-17 18:06:21 +0100 (Mon, 17 Sep
 *          2007) $
 */
public class AQTargetInterceptor implements Interceptor {

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
    private XAQueueConnectionFactory connectionFactory;

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

    /** Whether to process reponse */
    private final boolean processResp;

    private TransactionHandler transactionHandler;

    /**
     * @param methodName
     *            Method name.
     * @param destination
     *            Request destination.
     * @param connectionFactory
     *            Request connection factory.
     * @param correlationScheme
     *            Correlation scheme.
     * @param messageReceiver
     *            Message receiver for response.
     */
    public AQTargetInterceptor(String methodName, Destination destination, XAQueueConnectionFactory connectionFactory,
            CorrelationScheme correlationScheme, Fabric3MessageReceiver messageReceiver, ClassLoader classLoader, boolean processResp, TransactionHandler transactionHandler) {
        this.methodName = methodName;
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.correlationScheme = correlationScheme;
        this.messageReceiver = messageReceiver;
        this.cl = classLoader;
        this.processResp = processResp;
        this.transactionHandler = transactionHandler;
    }   

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message message) {

        final Message response = new MessageImpl();
        XAConnection connection = null;

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {

            Thread.currentThread().setContextClassLoader(cl);

            connection = connectionFactory.createXAConnection();
            XASession session = connection.createXASession();
            MessageProducer producer = session.createProducer(destination);
            Object[] payload = (Object[]) message.getBody();
            javax.jms.Message jmsMessage = session.createObjectMessage(payload);
            jmsMessage.setStringProperty("scaOperationName", methodName);
            
            transactionHandler.enlist(session);            
            producer.send(jmsMessage);
            System.err.println("SENDING Message"); 
            String correlationId = null;
            switch (correlationScheme) {
            case None:
                correlationId = "";
            case RequestCorrelIDToCorrelID:
                throw new UnsupportedOperationException("COrrelation scheme not supported");
            case RequestMsgIDToCorrelID:
                correlationId = jmsMessage.getJMSMessageID();
            }
            transactionHandler.commit();
            
            if (processResp) {
                ObjectMessage responseMessage = (ObjectMessage) messageReceiver.receive(correlationId);
                response.setBody(responseMessage.getObject());
            }
            return response;
        } catch (JMSException ex) {
            throw new Fabric3AQException("Unable to receive response", ex);
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

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }
}
