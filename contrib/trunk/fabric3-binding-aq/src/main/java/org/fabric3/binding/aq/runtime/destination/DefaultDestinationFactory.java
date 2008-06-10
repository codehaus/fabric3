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

package org.fabric3.binding.aq.runtime.destination;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

import org.fabric3.binding.aq.common.DestinationDefinition;
import org.fabric3.binding.aq.common.Fabric3AQException;
import org.fabric3.binding.aq.runtime.helper.JmsHelper;

/**
 * Default implementation for {@link DestinationFactory}
 */
public class DefaultDestinationFactory implements DestinationFactory<ConnectionFactory> {

    /**
     * Creates a destination from a {@link DestinationDefinition} and {@link ConnectionFactory}
     */
    public Destination getDestination(final DestinationDefinition definition, final ConnectionFactory connectionFactory) {        
        final String name = definition.getName();        
        final Destination queue;
        Connection connection = null;      
        try {                      
            connection = connectionFactory.createConnection();
            queue = connection.createSession(true, Session.AUTO_ACKNOWLEDGE).createQueue(name);            
            return queue;
        } catch(JMSException ex) {
            throw new Fabric3AQException("Unable to create destination", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
        
    }

}
