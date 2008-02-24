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

package org.fabric3.binding.aq.connectionfactory;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;

import oracle.jms.AQjmsFactory;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.model.AQBindingMetadata;

/**
 * The connection factory is never looked up, it is always created.
 * 
 * @version $Revision$ $Date: 2007-09-15 13:05:03 +0100 (Sat, 15 Sep
 *          2007) $
 * 
 */
public class CreateConnectionFactory implements ConnectionFactoryStrategy {
   

    /**
     * @see org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy#getConnectionFactory(java.lang.String)
     */
    public QueueConnectionFactory getConnectionFactory(final AQBindingMetadata metadata) {
        final QueueConnectionFactory connectionFactory;        
            try {
                connectionFactory =  AQjmsFactory.getQueueConnectionFactory(metadata.getDataSource());
            } catch (JMSException je) {
                throw new Fabric3AQException("Unable to create AQ connection factory ", je);
            }           
        return connectionFactory;
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

package org.fabric3.binding.aq.connectionfactory;

import javax.jms.JMSException;
import javax.jms.QueueConnectionFactory;

import oracle.jms.AQjmsFactory;

import org.fabric3.binding.aq.Fabric3AQException;
import org.fabric3.binding.aq.model.AQBindingMetadata;

/**
 * The connection factory is never looked up, it is always created.
 * 
 * @version $Revision$ $Date: 2007-09-15 13:05:03 +0100 (Sat, 15 Sep
 *          2007) $
 * 
 */
public class CreateConnectionFactory implements ConnectionFactoryStrategy {
   

    /**
     * @see org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy#getConnectionFactory(java.lang.String)
     */
    public QueueConnectionFactory getConnectionFactory(final AQBindingMetadata metadata) {
        final QueueConnectionFactory connectionFactory;        
            try {
                connectionFactory =  AQjmsFactory.getQueueConnectionFactory(metadata.getDataSource());
            } catch (JMSException je) {
                throw new Fabric3AQException("Unable to create AQ connection factory ", je);
            }           
        return connectionFactory;
    }
}
