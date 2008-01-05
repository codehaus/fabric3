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
package org.fabric3.binding.hessian.model.physical;

import java.net.URI;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.hessian.model.logical.HessianBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * Implementation of the hessian binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianBindingGenerator implements BindingGenerator<HessianWireSourceDefinition, HessianWireTargetDefinition, HessianBindingDefinition> {
    private ClassLoaderGenerator classLoaderGenerator;
    private GeneratorRegistry generatorRegistry;

    public HessianBindingGenerator(@Reference ClassLoaderGenerator classLoaderGenerator,
                                   @Reference GeneratorRegistry generatorRegistry) {
        this.classLoaderGenerator = classLoaderGenerator;
        this.generatorRegistry = generatorRegistry;
    }

    @Init
    public void start() {
        generatorRegistry.register(HessianBindingDefinition.class, this);
    }

    public HessianWireSourceDefinition generateWireSource(LogicalBinding<HessianBindingDefinition> logicalBinding,
                                                          Set<Intent> intentsToBeProvided,
                                                          Set<PolicySet> policySetsToBeProvided,
                                                          GeneratorContext generatorContext,
                                                          ServiceDefinition serviceDefinition)
            throws GenerationException {

        URI id = classLoaderGenerator.generate(logicalBinding, generatorContext);
        HessianWireSourceDefinition hwsd = new HessianWireSourceDefinition(id);
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwsd;

    }

    public HessianWireTargetDefinition generateWireTarget(LogicalBinding<HessianBindingDefinition> logicalBinding,
                                                          Set<Intent> intentsToBeProvided,
                                                          Set<PolicySet> policySetsToBeProvided,
                                                          GeneratorContext generatorContext,
                                                          ReferenceDefinition referenceDefinition)
            throws GenerationException {

        URI id = classLoaderGenerator.generate(logicalBinding, generatorContext);
        HessianWireTargetDefinition hwtd = new HessianWireTargetDefinition(id);
        hwtd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwtd;

    }

}
