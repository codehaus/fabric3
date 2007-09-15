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
package org.fabric3.binding.jms.host.standalone;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.host.JmsHost;
import org.fabric3.binding.jms.tx.TransactionHandler;
import org.osoa.sca.annotations.Destroy;

/**
 * Service handler for JMS.
 * 
 * @version $Revsion$ $Date: 2007-05-22 00:19:04 +0100 (Tue, 22 May 2007) $
 */
public class StandaloneJmsHost implements JmsHost {

    /**
     * Receiver connection.
     */
    private Connection connection;

    /**
     * Server session pools.
     */
    private List<StandaloneServerSessionPool> serverSessionPools = new CopyOnWriteArrayList<StandaloneServerSessionPool>();

    /**
     * Connection consumers.
     */
    private List<ConnectionConsumer> connectionConsumers = new CopyOnWriteArrayList<ConnectionConsumer>();

    /**
     * Stops the receiver threads.
     * @throws JMSException 
     */
    @Destroy
    public void stop() throws JMSException {
        for(StandaloneServerSessionPool sessionPool : serverSessionPools) {
            sessionPool.stop();
        }
        for(ConnectionConsumer connectionConsumer : connectionConsumers) {
            connectionConsumer.close();
        }
    }

    /**
     * @see org.fabric3.binding.jms.host.JmsHost#registerListener(javax.jms.Destination, 
     *                                                            javax.jms.ConnectionFactory, 
     *                                                            java.util.List, 
     *                                                            org.fabric3.binding.jms.tx.TransactionHandler)
     */
    public void registerListener(Destination destination, 
                                 ConnectionFactory connectionFactory, 
                                 List<MessageListener> listeners, 
                                 final TransactionHandler transactionHandler) {
        
        try {

            connection = connectionFactory.createConnection();
            List<Session> sessions = new LinkedList<Session>();
            for (final MessageListener listener : listeners) {
                final Session session = transactionHandler.createSession(connection);
                final MessageConsumer consumer = session.createConsumer(destination);
                // TODO Use the work scheduler
                new Thread(new ConsumerWorker(session, transactionHandler, consumer,listener)).start();
                sessions.add(session);
            }
            
            connection.start();
            
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to activate service", ex);
        }
        
    }

}
