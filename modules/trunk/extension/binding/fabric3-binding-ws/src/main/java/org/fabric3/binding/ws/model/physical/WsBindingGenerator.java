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
package org.fabric3.binding.ws.model.physical;

import java.util.Map;

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the WS binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class WsBindingGenerator implements BindingGenerator<PhysicalWireSourceDefinition, PhysicalWireTargetDefinition, WsBindingDefinition> {
    private Map<String, BindingGeneratorDelegate<WsBindingDefinition>> delegates;
    private GeneratorRegistry generatorRegistry;

    @Reference
    public void setGeneratorRegistry(GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @Reference
    public void setDelegates(Map<String, BindingGeneratorDelegate<WsBindingDefinition>> delegates) {
        this.delegates = delegates;
    }

    @Init
    public void start() {
        generatorRegistry.register(WsBindingDefinition.class, this);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                           Policy policy,
                                                           ServiceDefinition serviceDefinition)
            throws GenerationException {
        return getDelegate(logicalBinding).generateWireSource(logicalBinding, policy, serviceDefinition);
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> logicalBinding,
                                                           Policy policy,
                                                           ReferenceDefinition referenceDefinition)
            throws GenerationException {
        return getDelegate(logicalBinding).generateWireTarget(logicalBinding, policy, referenceDefinition);
    }

    /*
     * Gets the delegate for the requested WS stack. If no specific implementation is specified, the first registered
     * stack is returned.
     */
    private BindingGeneratorDelegate<WsBindingDefinition> getDelegate(LogicalBinding<WsBindingDefinition> logicalBinding)
            throws WsBindingGenerationException {

        if (delegates.isEmpty()) {
            throw new MissingWebServiceExtensionException();
        }
        String implementation = logicalBinding.getBinding().getImplementation();
        BindingGeneratorDelegate<WsBindingDefinition> delegate;
        if (implementation == null) {
            delegate = delegates.values().iterator().next();
        } else {
            delegate = delegates.get(implementation);
        }
        if (delegate == null) {
            throw new WsBindingGenerationException("Unknown web services extension requested", implementation);
        }
        return delegate;

    }

}
