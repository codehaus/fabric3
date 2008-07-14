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

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.binding.aq.common.Fabric3AQException;
import org.fabric3.binding.aq.runtime.helper.JmsHelper;

/**
 * XA Destination Factory
 */
public class XADestinationFactory implements DestinationFactory<XAQueueConnectionFactory> {

    /**
     * Creates a destination from a {@link DestinationDefinition} and {@link XAQueueConnectionFactory}
     */
    public Destination getDestination(String destinationName, XAQueueConnectionFactory connectionFactory) {   

        XAConnection connection = null;      
        try {                      
            connection = connectionFactory.createXAConnection();
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE).createQueue(destinationName);      
        } catch(JMSException ex) {
            throw new Fabric3AQException("Unable to create destination", ex);
        } finally {
            JmsHelper.closeQuietly(connection);
        }
        
    }

}
