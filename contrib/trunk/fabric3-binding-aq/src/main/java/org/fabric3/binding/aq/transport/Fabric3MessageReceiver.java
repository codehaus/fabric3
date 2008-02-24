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

package org.fabric3.binding.aq.transport;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;

/**
 * Message listeher for service requests.
 * 
 * @version $Revison$ $Date$
 */
public class Fabric3MessageReceiver {

    /**
     * Destination for receiving response.
     */
    private Destination destination;

    /**
     * Connection factory for receiving response.
     */
    private ConnectionFactory connectionFactory;

    /**
     * @param destination Destination for sending responses.
     * @param connectionFactory Connection factory for sending responses.
     */
    public Fabric3MessageReceiver(Destination destination,
                                  ConnectionFactory connectionFactory) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Performs a blocking receive.
     * 
     * @param correlationId Correlation Id.
     * @return Received message.
     */
    public Message receive(String correlationId) {
        
        /** Selector to consume the message */
        final String MSSG_SELECTOR = "MsgIDToCorrlID = '" + correlationId + "'";       
        Connection connection = null;
        
        try {
            
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
                                 
            MessageConsumer consumer = session.createConsumer(destination, MSSG_SELECTOR);            
            connection.start();

            Message message = consumer.receive();
            session.commit();
            
            return message;
            
        } catch(JMSException ex) {
            throw new Fabric3AQException("Unable to receive response", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
        
    }

}
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

package org.fabric3.binding.aq.transport;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.helper.JmsHelper;

/**
 * Message listeher for service requests.
 * 
 * @version $Revison$ $Date$
 */
public class Fabric3MessageReceiver {

    /**
     * Destination for receiving response.
     */
    private Destination destination;

    /**
     * Connection factory for receiving response.
     */
    private ConnectionFactory connectionFactory;

    /**
     * @param destination Destination for sending responses.
     * @param connectionFactory Connection factory for sending responses.
     */
    public Fabric3MessageReceiver(Destination destination,
                                  ConnectionFactory connectionFactory) {
        this.destination = destination;
        this.connectionFactory = connectionFactory;
    }

    /**
     * Performs a blocking receive.
     * 
     * @param correlationId Correlation Id.
     * @return Received message.
     */
    public Message receive(String correlationId) {
        
        /** Selector to consume the message */
        final String MSSG_SELECTOR = "MsgIDToCorrlID = '" + correlationId + "'";       
        Connection connection = null;
        
        try {
            
            connection = connectionFactory.createConnection();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
                                 
            MessageConsumer consumer = session.createConsumer(destination, MSSG_SELECTOR);            
            connection.start();

            Message message = consumer.receive();
            session.commit();
            
            return message;
            
        } catch(JMSException ex) {
            throw new Fabric3AQException("Unable to receive response", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
        
    }

}
