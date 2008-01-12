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
package org.fabric3.binding.rmi.model.physical;

import org.fabric3.binding.rmi.model.logical.RmiBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

@EagerInit
public class RmiBindingGenerator
        implements BindingGenerator<RmiWireSourceDefinition, RmiWireTargetDefinition, RmiBindingDefinition> {

    private final ClassLoaderGenerator classLoaderGenerator;

    /**
     * Injects the generator registry.
     *
     * @param generatorRegistry Generator registry.
     */
    public RmiBindingGenerator(
            @Reference GeneratorRegistry generatorRegistry, @Reference ClassLoaderGenerator classLoaderGenerator) {
        generatorRegistry.register(RmiBindingDefinition.class, this);
        this.classLoaderGenerator = classLoaderGenerator;
    }

    public RmiWireSourceDefinition generateWireSource(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            GeneratorContext generatorContext,
            ServiceDefinition serviceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to physical
        // TODO ignoring intents for now
        RmiWireSourceDefinition ewsd = new RmiWireSourceDefinition();
        ewsd.setUri(logicalBinding.getBinding().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getBinding());
        JavaServiceContract contract = JavaServiceContract.class.cast(serviceDefinition.getServiceContract());
        ewsd.setInterfaceName(contract.getInterfaceClass());
        ewsd.setClassLoaderURI(classLoaderGenerator.generate(logicalBinding, generatorContext));
        return ewsd;

    }

    public RmiWireTargetDefinition generateWireTarget(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            GeneratorContext generatorContext,
            ReferenceDefinition referenceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to the physical

        RmiWireTargetDefinition ewtd = new RmiWireTargetDefinition();
        ewtd.setUri(logicalBinding.getBinding().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getBinding());
        JavaServiceContract contract = JavaServiceContract.class.cast(referenceDefinition.getServiceContract());
        ewtd.setInterfaceName(contract.getInterfaceClass());
        ewtd.setClassLoaderURI(classLoaderGenerator.generate(logicalBinding, generatorContext));
        return ewtd;

    }

}
