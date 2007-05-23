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

package org.fabric3.binding.jms.wire.lookup.connectionfactory;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.naming.NameNotFoundException;

import org.fabric3.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.wire.helper.JndiHelper;

/**
 * The destination is looked up, if not found it is created.
 *
 */
public class IfNotExistConnectionFactoryStrategy implements ConnectionFactoryStrategy {
    
    /** Always strategy. */
    private ConnectionFactoryStrategy always = new AlwaysConnectionFactoryStrategy();

    /**
     * @see org.fabric3.binding.jms.wire.lookup.connectionfactory.ConnectionFactoryStrategy#getConnectionFactory(org.fabric3.binding.jms.model.ConnectionFactoryDefinition, java.util.Hashtable)
     */
    public ConnectionFactory getConnectionFactory(ConnectionFactoryDefinition definition, Hashtable<String, String> env) {
        try {
            return (ConnectionFactory) JndiHelper.lookup(definition.getName(), env);
        } catch(NameNotFoundException ex) {
            return always.getConnectionFactory(definition, env);
        }
        
    }

}
