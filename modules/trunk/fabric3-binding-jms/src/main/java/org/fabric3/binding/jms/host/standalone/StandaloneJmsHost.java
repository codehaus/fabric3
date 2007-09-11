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

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.host.JmsHost;
import org.fabric3.binding.jms.wire.helper.JmsHelper;
import org.osoa.sca.annotations.Property;

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
     * Sessions.
     */
    private List<Session> sessions = new LinkedList<Session>();
    
    /**
     * Receiver threads.
     */
    private int receiverThreads = 10;
    
    /**
     * Sets the number of receiver threads.
     * 
     * @param receiverThreads Number of receiver threads.
     */
    @Property 
    public void setReceiverThreads(int receiverThreads) {
        this.receiverThreads = receiverThreads;
    }

    /**
     * Stops the receiver threads.
     */
    public void stop() {

        for (Session session : sessions) {
            JmsHelper.closeQuietly(session);
        }
        JmsHelper.closeQuietly(connection);

    }

    /**
     * @see org.fabric3.binding.jms.host.JmsHost#registerListener(javax.jms.Destination, javax.jms.ConnectionFactory, javax.jms.MessageListener, boolean)
     */
    public void registerListener(Destination destination, 
                                 ConnectionFactory connectionFactory, 
                                 MessageListener messageListener, 
                                 boolean transactional) {
        
        try {

            connection = connectionFactory.createConnection();
            for (int i = 0; i < receiverThreads; i++) {
                Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
                MessageConsumer consumer = session.createConsumer(destination);
                consumer.setMessageListener(messageListener);
                sessions.add(session);
            }
            connection.start();
        } catch (JMSException ex) {
            throw new Fabric3JmsException("Unable to activate service", ex);
        }
        
    }

}
