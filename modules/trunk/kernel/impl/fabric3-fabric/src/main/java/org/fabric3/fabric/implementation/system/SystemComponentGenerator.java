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

import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ValueSource;
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
import org.fabric3.spi.policy.Policy;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

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
        PojoComponentType type = implementation.getComponentType();

        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setConstructor(type.getConstructorDefinition().getSignature());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        SystemComponentDefinition physical = new SystemComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());
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
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {

        URI uri = reference.getUri();
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.REFERENCE, uri.getFragment()));

        URI classLoaderId = classLoaderGenerator.generate(source, context);
        wireDefinition.setClassLoaderId(classLoaderId);

        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<SystemImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy,
                                                                   GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<SystemImplementation> logical,
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        SystemWireTargetDefinition wireDefinition = new SystemWireTargetDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(service.getUri());
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<SystemImplementation> source,
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context) throws GenerationException {
        URI uri = resource.getUri();
        SystemWireSourceDefinition wireDefinition = new SystemWireSourceDefinition();
        wireDefinition.setOptimizable(true);
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.RESOURCE, uri.getFragment()));

        URI classLoaderId = classLoaderGenerator.generate(source, context);
        wireDefinition.setClassLoaderId(classLoaderId);

        return wireDefinition;
    }


}
