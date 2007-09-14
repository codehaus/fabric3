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

package org.fabric3.binding.jms.lookup.connectionfactory;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.model.ConnectionFactoryDefinition;

/**
 * The connection factory is never looked up, it is always created.
 * 
 * @version $Revision$ $Date$
 *
 */
public class AlwaysConnectionFactoryStrategy implements ConnectionFactoryStrategy {

    /**
     * @see org.fabric3.binding.jms.lookup.connectionfactory.ConnectionFactoryStrategy#getConnectionFactory(org.fabric3.binding.jms.model.ConnectionFactoryDefinition, java.util.Hashtable)
     */
    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) {
        
        try {            
            return (ConnectionFactory) Class.forName(definition.getName()).newInstance();            
        } catch (InstantiationException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (IllegalAccessException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        } catch (ClassNotFoundException ex) {
            throw new Fabric3JmsException("Unable to create connection factory", ex);
        }
        
    }

}
