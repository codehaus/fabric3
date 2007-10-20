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
package org.fabric3.binding.jms.model;

import org.fabric3.scdl.ModelObject;

/**
 * @version $Revision$ $Date$
 */
public class ResponseDefinition extends ModelObject {

    /**
     * Destination.
     */
    private DestinationDefinition destination;

    /**
     * Connection factory.
     */
    private ConnectionFactoryDefinition connectionFactory;

    /**
     * @see org.fabric3.binding.jms.model.ConnectionFactoryAware#getConnectionFactory()
     */
    public ConnectionFactoryDefinition getConnectionFactory() {
        return connectionFactory;
    }

    /**
     * @see org.fabric3.binding.jms.model.ConnectionFactoryAware#setConnectionFactory(org.fabric3.binding.jms.model.ConnectionFactoryDefinition)
     */
    public void setConnectionFactory(ConnectionFactoryDefinition connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /**
     * @see org.fabric3.binding.jms.model.DestinationAware#getDestination()
     */
    public DestinationDefinition getDestination() {
        return destination;
    }

    /**
     * @see org.fabric3.binding.jms.model.DestinationAware#setDestination(org.fabric3.binding.jms.model.DestinationDefinition)
     */
    public void setDestination(DestinationDefinition destination) {
        this.destination = destination;
    }

}
