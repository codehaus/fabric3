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
package org.fabric3.fabric.implementation.system;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.ComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemComponentGenerator implements ComponentGenerator<LogicalComponent<SystemImplementation>> {
    private final InstanceFactoryGenerationHelper helper;

    public SystemComponentGenerator(@Reference GeneratorRegistry registry,
                                    @Reference InstanceFactoryGenerationHelper helper) {
        registry.register(SystemImplementation.class, this);
        this.helper = helper;
    }

    public void generate(LogicalComponent<SystemImplementation> component, GeneratorContext context) {
        ComponentDefinition<SystemImplementation> definition = component.getDefinition();
        SystemImplementation implementation = definition.getImplementation();
        @SuppressWarnings({"unchecked"})
        PojoComponentType type = implementation.getComponentType();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setInitMethod(helper.getSignature(type.getInitMethod()));
        providerDefinition.setDestroyMethod(helper.getSignature(type.getDestroyMethod()));
        providerDefinition.setImplementationClass(implementation.getImplementationClass().getName());
        helper.processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        helper.processConstructorSites(type, providerDefinition);
        helper.processReferenceSites(type, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        SystemComponentDefinition physical = new SystemComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(componentId.resolve("."));
        // TODO set the classloader id temporarily until multiparent classloading is in palce
        physical.setClassLoaderId(URI.create("sca://./bootClassLoader"));
        physical.setScope(type.getImplementationScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processProperties(physical, definition);

        context.getPhysicalChangeSet().addComponentDefinition(physical);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<SystemImplementation> source,
                                                           LogicalReference reference,
                                                           boolean optimizable, GeneratorContext context)
            throws GenerationException {
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        wireDefinition.setOptimizable(true);
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<SystemImplementation> logical,
                                                           GeneratorContext context) throws GenerationException {
        SystemWireTargetDefinition wireDefinition = new SystemWireTargetDefinition();
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }


}
