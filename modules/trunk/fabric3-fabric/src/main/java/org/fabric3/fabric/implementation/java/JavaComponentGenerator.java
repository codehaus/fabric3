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
package org.fabric3.fabric.implementation.java;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.implementation.pojo.PojoGenerationHelper;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.implementation.java.PojoComponentType;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.ComponentDefinition;

/**
 * Generates a JavaComponentDefinition from a ComponentDefinition corresponding to a Java component implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    private final PojoGenerationHelper helper;
    private final ClassLoaderGenerator classLoaderGenerator;

    public JavaComponentGenerator(@Reference GeneratorRegistry registry,
                                  @Reference ClassLoaderGenerator classLoaderGenerator,
                                  @Reference PojoGenerationHelper helper) {
        this.classLoaderGenerator = classLoaderGenerator;
        registry.register(JavaImplementation.class, this);
        this.helper = helper;
    }

    public void generate(LogicalComponent<JavaImplementation> component, GeneratorContext context)
            throws GenerationException {
        ComponentDefinition<JavaImplementation> definition = component.getDefinition();
        JavaImplementation implementation = definition.getImplementation();
        @SuppressWarnings({"unchecked"})
        PojoComponentType type = implementation.getComponentType();

        // create the instance factory definition
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setInitMethod(helper.getSignature(type.getInitMethod()));
        providerDefinition.setDestroyMethod(helper.getSignature(type.getDestroyMethod()));
        providerDefinition.setImplementationClass(implementation.getImplementationClass().getName());
        helper.processConstructorArguments(type.getConstructorDefinition(), providerDefinition);
        helper.processConstructorSites(type, providerDefinition);
        helper.processReferenceSites(type, providerDefinition);
        helper.processCallbackSites(type, providerDefinition);
        // TODO process properties

        // create the physical component definition
        URI componentId = component.getUri();
        JavaComponentDefinition physical = new JavaComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(componentId.resolve("."));
        physical.setScope(type.getImplementationScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processProperties(physical, definition);
        // generate the classloader resource definition
        URI classLoaderId = classLoaderGenerator.generate(component, context);
        physical.setClassLoaderId(classLoaderId);
        context.getPhysicalChangeSet().addComponentDefinition(physical);
    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JavaImplementation> source,
                                                           LogicalReference reference,
                                                           boolean optimizable,
                                                           GeneratorContext context) throws GenerationException {
        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        wireDefinition.setOptimizable(optimizable);
        wireDefinition.setConversational(reference.getDefinition().getServiceContract().isConversational());
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<JavaImplementation> target,
                                                           GeneratorContext context)
            throws GenerationException {
        JavaWireTargetDefinition wireDefinition = new JavaWireTargetDefinition();
        URI uri;
        if (service != null) {
            uri = service.getUri();
        } else {
            // no service specified, use the default
            uri = target.getUri();
        }
        wireDefinition.setUri(uri);
        return wireDefinition;
    }


}
