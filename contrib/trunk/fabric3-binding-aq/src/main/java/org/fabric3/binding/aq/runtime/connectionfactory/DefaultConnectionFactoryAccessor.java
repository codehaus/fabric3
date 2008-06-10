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

package org.fabric3.binding.aq.runtime.connectionfactory;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import oracle.jms.AQjmsFactory;

import org.fabric3.binding.aq.common.AQBindingMetadata;
import org.fabric3.binding.aq.common.Fabric3AQException;

/**
 * Default implementation that gets the {@link ConnectionFactory} from the
 * {@link AQjmsFactory}
 * 
 * @version $Revision: 2902 $ $Date: 2007-09-15 13:05:03 +0100 (Sat, 15 Sep
 *          2007) $
 */
public class DefaultConnectionFactoryAccessor implements ConnectionFactoryAccessor<ConnectionFactory> {

    /**
     * Gets a QueueConnectionFactory from {@link AQjmsFactory}
     * 
     * @param metadata -
     *            meta information used to the QueueConnectionFactory
     * @return QueueConnectionFactory
     */
    public ConnectionFactory getConnectionFactory(final AQBindingMetadata metadata) {
        final ConnectionFactory connectionFactory;
        try {
            connectionFactory = AQjmsFactory.getConnectionFactory(metadata.getDataSource());            
        } catch (JMSException je) {
            throw new Fabric3AQException("Unable to create AQ connection factory ", je);
        }

        return connectionFactory;
    }

    
}
