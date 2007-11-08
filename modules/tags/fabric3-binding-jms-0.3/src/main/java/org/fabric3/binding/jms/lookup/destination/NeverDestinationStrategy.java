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

package org.fabric3.binding.jms.lookup.destination;

import java.util.Hashtable;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.naming.NameNotFoundException;

import org.fabric3.binding.jms.Fabric3JmsException;
import org.fabric3.binding.jms.helper.JndiHelper;
import org.fabric3.binding.jms.model.DestinationDefinition;

/**
 * The destination is always looked up and never created.
 *
 */
public class NeverDestinationStrategy implements DestinationStrategy {

    /**
     * @see org.fabric3.binding.jms.lookup.destination.DestinationStrategy#getDestination(org.fabric3.binding.jms.model.DestinationDefinition, javax.jms.ConnectionFactory, java.util.Hashtable)
     */
    public Destination getDestination(DestinationDefinition definition,
                                      ConnectionFactory cf,
                                      Hashtable<String, String> env) {
        try {
            return (Destination) JndiHelper.lookup(definition.getName(), env);
        } catch(NameNotFoundException ex) {
            throw new Fabric3JmsException(definition.getName() + " not found", ex);
        }
    }

}
