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
package org.fabric3.java;

import java.net.URI;

import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Generates a JavaComponentDefinition from a ComponentDefinition corresponding to a Java component implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    private final InstanceFactoryGenerationHelper helper;
    private final ClassLoaderGenerator classLoaderGenerator;

    public JavaComponentGenerator(@Reference GeneratorRegistry registry,
                                  @Reference ClassLoaderGenerator classLoaderGenerator,
                                  @Reference InstanceFactoryGenerationHelper helper) {
        this.classLoaderGenerator = classLoaderGenerator;
        registry.register(JavaImplementation.class, this);
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JavaImplementation> component,
                                                GeneratorContext context)
            throws GenerationException {
        ComponentDefinition<JavaImplementation> definition = component.getDefinition();
        JavaImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();

        // create the instance factory definition
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        JavaComponentDefinition physical = new JavaComponentDefinition();
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

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JavaImplementation> source,
                                                           LogicalReference reference,
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = classLoaderGenerator.generate(source, context);

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        wireDefinition.setConversational(serviceContract.isConversational());

        // assume for now that any wire from a Java component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<JavaImplementation> source,
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context) throws GenerationException {
        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = getInterfaceName(serviceContract);
        URI classLoaderId = classLoaderGenerator.generate(source, context);

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new ValueSource(ValueSource.ValueSourceType.RESOURCE, uri.getFragment()));
        wireDefinition.setConversational(false);
        wireDefinition.setClassLoaderId(classLoaderId);
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    private String getInterfaceName(ServiceContract<?> contract) {
        return contract.getQualifiedInterfaceName();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<JavaImplementation> target,  
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        URI uri;
        if (service != null) {
            uri = service.getUri();
        } else {
            // no service specified, use the default
            uri = target.getUri();
        }
        wireDefinition.setUri(uri);

        // assume for now that only wires to composite scope components can be optimized
        Scope<?> scope = target.getDefinition().getImplementation().getComponentType().getImplementationScope();
        wireDefinition.setOptimizable(Scope.COMPOSITE.equals(scope));
        return wireDefinition;
    }
}
