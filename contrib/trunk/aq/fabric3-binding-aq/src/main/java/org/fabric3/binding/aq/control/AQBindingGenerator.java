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
package org.fabric3.binding.aq.control;

import java.net.URI;

import org.fabric3.binding.aq.provision.AQWireSourceDefinition;
import org.fabric3.binding.aq.provision.AQWireTargetDefinition;
import org.fabric3.binding.aq.scdl.AQBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;

/**
 * Binding generator that creates the physical source and target definitions for wires. Message acknowledgement is
 * always expected to be using transactions, either local or global, as expressed by the intents transactedOneWay,
 * transactedOneWay.local or transactedOneWay.global.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class AQBindingGenerator implements BindingGenerator<AQWireSourceDefinition, AQWireTargetDefinition, AQBindingDefinition> {

    /**
     * Builds the Source Definition
     */
    public AQWireSourceDefinition generateWireSource(LogicalBinding<AQBindingDefinition> logicalBinding, 
                                                     Policy policy, 
                                                     ServiceDefinition serviceDefinition) {
        URI classLoaderId = logicalBinding.getParent().getParent().getParent().getUri();
        AQBindingDefinition bd = logicalBinding.getBinding();        
        return new AQWireSourceDefinition(bd.getDestinationName(), bd.getInitialState(), bd.getDataSourceKey(), bd.getConsumerCount(), classLoaderId);
    }

    /**
     * Builds the Target Definition
     */
    public AQWireTargetDefinition generateWireTarget(LogicalBinding<AQBindingDefinition> logicalBinding, 
                                                     Policy policy, 
                                                     ReferenceDefinition referenceDefinition)throws GenerationException {
        URI classLoaderId = logicalBinding.getParent().getParent().getParent().getUri();
        AQBindingDefinition bd = logicalBinding.getBinding();                
        return new AQWireTargetDefinition(bd.getDestinationName(), bd.getDataSourceKey(), classLoaderId);

    }

}
