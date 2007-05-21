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

package org.fabric3.binding.jms.wire.lookup;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.Session;
import javax.jms.TopicConnection;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.wire.helper.JmsHelper;

/**
 * The destination is never looked up, it is always created.
 *
 */
public class AlwaysDestinationStrategy implements DestinationStrategy {

    /**
     * @see org.fabric3.binding.jms.wire.lookup.DestinationStrategy#getDestination(org.fabric3.binding.jms.model.DestinationDefinition, javax.jms.ConnectionFactory, java.util.Hashtable)
     */
    public Destination getDestination(DestinationDefinition definition,
                                      ConnectionFactory cf,
                                      Hashtable<String, String> env) {
        
        Connection connection = null;
        
        try {
            
            String name = definition.getName();
            connection = cf.createConnection();
            
            switch(definition.getDestinationType()) {
                case queue:
                    QueueConnection qc = (QueueConnection) connection;
                    return qc.createQueueSession(false, Session.AUTO_ACKNOWLEDGE).createQueue(name);
                case topic:
                    TopicConnection tc = (TopicConnection) connection;
                    return tc.createTopicSession(false, Session.AUTO_ACKNOWLEDGE).createQueue(name);
                default:
                    throw new IllegalArgumentException("Unknown destination type");
            }
            
        } catch(JMSException ex) {
            throw new Fabric3JmsException("Unable to create destination", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
        
    }

}
