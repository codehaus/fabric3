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
package org.fabric3.binding.aq.wire;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;

import oracle.jms.AQjmsFactory;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.transport.Fabric3MessageReceiver;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

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

    /** Whether to process reponse */
    private final boolean processResp;

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
    public AQTargetInterceptor(String methodName, Destination destination, ConnectionFactory connectionFactory,
            CorrelationScheme correlationScheme, Fabric3MessageReceiver messageReceiver, ClassLoader classLoader, boolean processResp) {
        this.methodName = methodName;
        this.destination = destination;
        this.connectionFactory = connectionFactory;
        this.correlationScheme = correlationScheme;
        this.messageReceiver = messageReceiver;
        this.cl = classLoader;
        this.processResp = processResp;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message message) {

        final Message response = new MessageImpl();
        Connection connection = null;

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {

            Thread.currentThread().setContextClassLoader(cl);

            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);

            MessageProducer producer = session.createProducer(destination);

            Object[] payload = (Object[]) message.getBody();
            javax.jms.Message jmsMessage = session.createObjectMessage(payload);
            jmsMessage.setStringProperty("scaOperationName", methodName);

            producer.send(jmsMessage);

            String correlationId = null;
            switch (correlationScheme) {
            case None:
                correlationId = "";
            case RequestCorrelIDToCorrelID:
                throw new UnsupportedOperationException("COrrelation scheme not supported");
            case RequestMsgIDToCorrelID:
                correlationId = jmsMessage.getJMSMessageID();
            }
            session.commit();
            
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

}
