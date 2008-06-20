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
package org.fabric3.java.control;

import java.net.URI;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaComponentDefinition;
import org.fabric3.java.provision.JavaWireSourceDefinition;
import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.InstanceFactoryGenerationHelper;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.CallbackDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.Constants;
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
 * Generates a JavaComponentDefinition from a ComponentDefinition corresponding to a Java component implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaComponentGenerator implements ComponentGenerator<LogicalComponent<JavaImplementation>> {
    private static final QName PROPAGATES_CONVERSATION = new QName(Constants.FABRIC3_NS, "propagatesConversation");
    private final InstanceFactoryGenerationHelper helper;

    public JavaComponentGenerator(@Reference GeneratorRegistry registry, @Reference InstanceFactoryGenerationHelper helper) {
        registry.register(JavaImplementation.class, this);
        this.helper = helper;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<JavaImplementation> component) throws GenerationException {
        ComponentDefinition<JavaImplementation> definition = component.getDefinition();
        JavaImplementation implementation = definition.getImplementation();
        PojoComponentType type = implementation.getComponentType();

        // create the instance factory definition
        InstanceFactoryDefinition providerDefinition = new InstanceFactoryDefinition();
        providerDefinition.setConstructor(type.getConstructor());
        providerDefinition.setInitMethod(type.getInitMethod());
        providerDefinition.setDestroyMethod(type.getDestroyMethod());
        providerDefinition.setImplementationClass(implementation.getImplementationClass());
        helper.processInjectionSites(component, providerDefinition);

        // create the physical component definition
        URI componentId = component.getUri();
        JavaComponentDefinition physical = new JavaComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());
        physical.setScope(type.getScope());
        physical.setInitLevel(helper.getInitLevel(definition, type));
        physical.setMaxAge(type.getMaxAge());
        physical.setMaxIdleTime(type.getMaxIdleTime());
        physical.setInstanceFactoryProviderDefinition(providerDefinition);
        helper.processPropertyValues(component, physical);
        // generate the classloader resource definition
        URI classLoaderId = component.getClassLoaderId();
        physical.setClassLoaderId(classLoaderId);

        return physical;

    }

    public PhysicalWireSourceDefinition generateWireSource(LogicalComponent<JavaImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {
        URI uri = reference.getUri();
        ServiceContract<?> serviceContract = reference.getDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.REFERENCE, uri.getFragment()));
        wireDefinition.setInterfaceName(interfaceName);
        // assume for now that any wire from a Java component can be optimized
        wireDefinition.setOptimizable(true);

        wireDefinition.setClassLoaderId(classLoaderId);
        calculateConversationalPolicy(wireDefinition, serviceContract, policy);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<JavaImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();
        PojoComponentType type = source.getDefinition().getImplementation().getComponentType();
        String name = null;
        for (CallbackDefinition entry : type.getCallbacks().values()) {
            // NB: This currently only supports the case where one callback injection site of the same type is on an implementation.
            // TODO clarify with the spec if having more than one callback injection site of the same type is valid
            if (entry.getServiceContract().isAssignableFrom(serviceContract)) {
                name = entry.getName();
                break;
            }
        }
        if (name == null) {
            String interfaze = serviceContract.getQualifiedInterfaceName();
            throw new CallbackSiteNotFound("Callback injection site not found for type: " + interfaze, interfaze);
        }

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.CALLBACK, name));
        wireDefinition.setInterfaceName(interfaceName);
        wireDefinition.setUri(URI.create(source.getUri().toString() + "#" + name));
        wireDefinition.setOptimizable(false);
        wireDefinition.setClassLoaderId(classLoaderId);
        return wireDefinition;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<JavaImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        URI uri = resource.getUri();
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        String interfaceName = serviceContract.getQualifiedInterfaceName();
        URI classLoaderId = source.getClassLoaderId();

        JavaWireSourceDefinition wireDefinition = new JavaWireSourceDefinition();
        wireDefinition.setUri(uri);
        wireDefinition.setValueSource(new InjectableAttribute(InjectableAttributeType.RESOURCE, uri.getFragment()));
        wireDefinition.setClassLoaderId(classLoaderId);
        wireDefinition.setInterfaceName(interfaceName);
        return wireDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<JavaImplementation> target, Policy policy)
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
        URI classLoaderId = target.getClassLoaderId();
        wireDefinition.setClassLoaderId(classLoaderId);

        // assume for now that only wires to composite scope components can be optimized
        String scope = target.getDefinition().getImplementation().getComponentType().getScope();
        wireDefinition.setOptimizable("COMPOSITE".equals(scope));
        return wireDefinition;
    }

    private void calculateConversationalPolicy(JavaWireSourceDefinition wireDefinition, ServiceContract<?> serviceContract, Policy policy) {
        for (Operation<?> operation : serviceContract.getOperations()) {
            for (Intent intent : policy.getProvidedIntents(operation)) {
                if (PROPAGATES_CONVERSATION.equals(intent.getName())) {
                    wireDefinition.setInteractionType(InteractionType.PROPAGATES_CONVERSATION);
                    // conversational propagation is for the entire reference so set it an return
                    return;
                }
            }
        }
        if (serviceContract.isConversational()) {
            wireDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }

    }

}
