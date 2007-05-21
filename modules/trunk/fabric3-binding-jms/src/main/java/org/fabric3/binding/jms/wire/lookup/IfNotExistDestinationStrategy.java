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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.fabric3.binding.jms.model.DestinationDefinition;

/**
 * The destination is looked up, if not found it is created.
 *
 */
public class IfNotExistDestinationStrategy implements DestinationStrategy {
    
    /** Never strategy. */
    private DestinationStrategy never = new NeverDestinationStrategy();
    
    /** Always strategy. */
    private DestinationStrategy always = new AlwaysDestinationStrategy();

    /**
     * @see org.fabric3.binding.jms.wire.lookup.DestinationStrategy#getDestination(org.fabric3.binding.jms.model.DestinationDefinition, javax.jms.ConnectionFactory, java.util.Hashtable)
     */
    public Destination getDestination(DestinationDefinition definition,
                                      ConnectionFactory cf,
                                      Hashtable<String, String> env) {
        
        Destination destination = never.getDestination(definition, cf, env);
        if(destination == null) {
            destination = always.getDestination(definition, cf, env);
        }
        
        return destination;
        
    }

}
