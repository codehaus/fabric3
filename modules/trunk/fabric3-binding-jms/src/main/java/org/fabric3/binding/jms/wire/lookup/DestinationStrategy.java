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
 * Strategy for looking up destinations.
 * 
 * @version $Revsion$ $Date$
 *
 */
public interface DestinationStrategy {

    /**
     * Gets the destination based on SCA JMS binding rules.
     * 
     * @param definition Destination definition.
     * @param cf Connection factory.
     * @param env JNDI environment.
     * @return Lokked up or created destination.
     */
    Destination getDestination(DestinationDefinition definition, ConnectionFactory cf, Hashtable<String, String> env);

}
