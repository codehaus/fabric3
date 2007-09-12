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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.jms.Connection;
import javax.jms.ConnectionConsumer;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.TransactionType;
import org.fabric3.binding.jms.host.JmsHost;
import org.fabric3.binding.jms.tx.JmsTransactionHandler;
import org.fabric3.binding.jms.tx.JtaTransactionHandler;
import org.fabric3.binding.jms.tx.TransactionHandler;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;

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
     * Transaction handlers.
     */
    private Map<TransactionType, TransactionHandler> transactionHandlers = new HashMap<TransactionType, TransactionHandler>();
    
    /**
     * Injects the transaction handlers.
     * @param txHandlers Transaction handlers.
     * TODO Fix this when map support is enabled in the system tree.
     */
    /*@Reference
    public void setTransactionHandlers(Map<String, TransactionHandler> txHandlers) {
        for(Map.Entry<String, TransactionHandler> entry: txHandlers.entrySet()) {
            transactionHandlers.put(TransactionType.valueOf(entry.getKey()), entry.getValue());
        }
    }*/
    
    @Reference
    public void setJmsTransactionHandler(JmsTransactionHandler transactionHandler) {
        transactionHandlers.put(TransactionType.LOCAL, transactionHandler);
    }
    
    @Reference
    public void setJtaTransactionHandler(JtaTransactionHandler transactionHandler) {
        transactionHandlers.put(TransactionType.GLOBAL, transactionHandler);
    }

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
     * @see org.fabric3.binding.jms.host.JmsHost#registerListener(javax.jms.Destination, javax.jms.ConnectionFactory, javax.jms.MessageListener, boolean)
     */
    public void registerListener(Destination destination, 
                                 ConnectionFactory connectionFactory, 
                                 List<MessageListener> listeners, 
                                 TransactionType transactionType) {
        
        try {

            connection = connectionFactory.createConnection();
            List<Session> sessions = new LinkedList<Session>();
            for (MessageListener listener : listeners) {
                boolean transacted = transactionType != TransactionType.GLOBAL;
                Session session = connection.createSession(transacted, Session.SESSION_TRANSACTED);
                session.setMessageListener(listener);
                sessions.add(session);
            }
            
            TransactionHandler transactionHandler = transactionHandlers.get(transactionType);
            StandaloneServerSessionPool serverSessionPool = new StandaloneServerSessionPool(sessions, transactionHandler);
            
            ConnectionConsumer connectionConsumer = connection.createConnectionConsumer(destination, null, serverSessionPool, 1);
            serverSessionPools.add(serverSessionPool);
            connectionConsumers.add(connectionConsumer);
            
            connection.start();
            
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to activate service", ex);
        }
        
    }

}
