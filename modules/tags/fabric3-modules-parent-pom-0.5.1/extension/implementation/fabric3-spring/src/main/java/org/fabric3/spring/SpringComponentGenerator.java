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
package org.fabric3.spring;

import java.net.URI;
import java.util.Iterator;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Generates a SpringComponentDefinition from a ComponentDefinition corresponding to a Spring component implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class SpringComponentGenerator implements ComponentGenerator<LogicalComponent<SpringImplementation>> {


    public SpringComponentGenerator(@Reference GeneratorRegistry registry) {

        registry.register(SpringImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<SpringImplementation> component)
            throws GenerationException {
        ComponentDefinition<SpringImplementation> componentDefinition = component.getDefinition();

        SpringImplementation implementation = componentDefinition.getImplementation();
        SpringComponentType type = implementation.getComponentType();

        type.getReferences().putAll(componentDefinition.getReferences());

        // create the physical component definition
        URI componentId = component.getUri();
        SpringComponentDefinition physical = new SpringComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());
        physical.setScope(type.getScope());

        // generate the classloader resource definition
        URI classLoaderId = component.getClassLoaderId();
        physical.setClassLoaderId(classLoaderId);

        // For Spring component: service name = spring bean id (name)
        // TODO Need to go through the whole list
        Iterator<String> i = componentDefinition.getServices().keySet().iterator();
        String springBeanId = null;
        if (i.hasNext())
            springBeanId = i.next();
        physical.setSpringBeanId(springBeanId);

        physical.setReferences(type.getReferences());

        // resource is application-context.xml file
        physical.setResource(implementation.getResource());

        return physical;

    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<SpringImplementation> source,
                                                           LogicalReference reference,
                                                           Policy policy) throws GenerationException {
        SpringWireSourceDefinition wireDefinition = new SpringWireSourceDefinition();
        wireDefinition.setUri(reference.getUri());
        boolean conversational = reference.getDefinition().getServiceContract().isConversational();
        if (conversational) {
            wireDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }
        URI classLoaderId = source.getClassLoaderId();
        wireDefinition.setClassLoaderId(classLoaderId);

        ComponentDefinition<SpringImplementation> componentDefinition = source.getDefinition();
        SpringImplementation implementation = componentDefinition.getImplementation();
        Class<?> fieldType = implementation.getFieldType(reference.getDefinition().getName());
        wireDefinition.setFieldType(fieldType);

        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<SpringImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<SpringImplementation> target,
                                                           Policy policy) throws GenerationException {
        SpringWireTargetDefinition wireDefinition = new SpringWireTargetDefinition();
        URI uri;
        if (service != null) {
            uri = service.getUri();
        } else {
            // no service specified, use the default
            uri = target.getUri();
        }

        ComponentDefinition<SpringImplementation> componentDefinition = target.getDefinition();
        SpringImplementation implementation = componentDefinition.getImplementation();
        String beanId = implementation.getBeanId(service.getDefinition().getName());

        wireDefinition.setBeanId(beanId);
        wireDefinition.setUri(uri);
        return wireDefinition;
    }

    /**
     * @see org.fabric3.spi.generator.ComponentGenerator#generateResourceWireSource(org.fabric3.spi.model.instance.LogicalComponent,
     *      org.fabric3.spi.model.instance.LogicalResource)
     */
    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<SpringImplementation> source,
                                                                   LogicalResource<?> resource) throws GenerationException {
        SpringWireSourceDefinition wireDefinition = new SpringWireSourceDefinition();
        wireDefinition.setUri(resource.getUri());
        return wireDefinition;
    }


}
