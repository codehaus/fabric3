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

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.QueueConnectionFactory;

import org.fabric3.binding.aq.common.DestinationDefinition;

/**
 * Factory used to create the Jms Destinations\
 * @version $Revsion$ $Date$
 */
public interface DestinationFactory<CF extends ConnectionFactory> {

  
    /**
     * Gets the Queue from the a {@link DestinationDefinition} and {@link QueueConnectionFactory}
     * @param definition - Meta information used to construct the destination
     * @param connetcionFactory - connection used to get Queue from
     * @return return the created {@link Destination}
     */
    Destination getDestination(DestinationDefinition definition, CF connectionFactory);

}
