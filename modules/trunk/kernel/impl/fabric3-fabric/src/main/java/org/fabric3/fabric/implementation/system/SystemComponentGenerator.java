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
import java.util.Set;

import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemComponentGenerator implements ComponentGenerator<LogicalComponent<SystemImplementation>> {
    private final InstanceFactoryGenerationHelper helper;
    private ClassLoaderGenerator classLoaderGenerator;

    public SystemComponentGenerator(@Reference GeneratorRegistry registry,
                                    @Reference ClassLoaderGenerator classLoaderGenerator,
                                    @Reference InstanceFactoryGenerationHelper helper) {
        this.classLoaderGenerator = classLoaderGenerator;
        registry.register(SystemImplementation.class, this);
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<SystemImplementation> component,
                                                GeneratorContext context)
            throws GenerationException {
        ComponentDefinition<SystemImplementation> definition = component.getDefinition();
        SystemImplementation implementation = definition.getImplementation();
        @SuppressWarnings({"unchecked"})
        PojoComponentType type = implementation.getComponentType();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        SystemComponentDefinition physical = new SystemComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(componentId.resolve("."));
        physical.setScope(type.getImplementationScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);

        // generate the classloader resource definition
        URI classLoaderId = classLoaderGenerator.generate(component, context);
        physical.setClassLoaderId(classLoaderId);

        return physical;
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<SystemImplementation> source,
                                                           LogicalReference reference,
                                                           boolean optimizable,  
                                                           Set<Intent> intentsToBeProvided,
                                                           Set<Element> policySetsToBeProvided,
                                                           GeneratorContext context) throws GenerationException {
        
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        wireDefinition.setOptimizable(true);

        URI classLoaderId = classLoaderGenerator.generate(source, context);
        wireDefinition.setClassLoaderId(classLoaderId);
        
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, 
                                                           LogicalComponent<SystemImplementation> logical,  
                                                           Set<Intent> intentsToBeProvided,
                                                           Set<Element> policySetsToBeProvided,
                                                           GeneratorContext context) throws GenerationException {
        SystemWireTargetDefinition wireDefinition = new SystemWireTargetDefinition();
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<SystemImplementation> source, 
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }


}
