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
package org.fabric3.runtime.webapp.implementation.webapp;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
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
public class WebappComponentGenerator implements ComponentGenerator<LogicalComponent<WebappImplementation>> {

    public WebappComponentGenerator(@Reference GeneratorRegistry registry) {
        registry.register(WebappImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<WebappImplementation> component,
                                                GeneratorContext context) {
        ComponentDefinition<WebappImplementation> definition = component.getDefinition();
        AbstractComponentType<ServiceDefinition, ReferenceDefinition, Property<?>, ResourceDefinition> componentType =
                definition.getImplementation().getComponentType();

        WebappComponentDefinition pDefinition = new WebappComponentDefinition();
        URI componentId = component.getUri();
        pDefinition.setComponentId(componentId);
        pDefinition.setGroupId(component.getParent().getUri());

        Map<String, Class<?>> referenceTypes = new HashMap<String, Class<?>>();
        for (ReferenceDefinition referenceDefinition : componentType.getReferences().values()) {
            String name = referenceDefinition.getName();
            // JFM is this correct to assume?
            ServiceContract<?> contract = referenceDefinition.getServiceContract();
            String interfaceClass = contract.getQualifiedInterfaceName();
            try {
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> type = Class.forName(interfaceClass, true, loader);
                referenceTypes.put(name, type);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
        }
        pDefinition.setReferenceTypes(referenceTypes);
        context.getPhysicalChangeSet().addComponentDefinition(pDefinition);
        
        return pDefinition;
    }

    public WebappWireSourceDefinition generateWireSource(LogicalComponent<WebappImplementation> source,
                                                         LogicalReference reference,
                                                         Policy policy,
                                                         GeneratorContext context) throws GenerationException {

        WebappWireSourceDefinition sourceDefinition = new WebappWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        return sourceDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, 
                                                           LogicalComponent<WebappImplementation> arg1,  
                                                           Policy policy,
                                                           GeneratorContext context) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<WebappImplementation> source, 
                                                                   LogicalResource<?> resource,
                                                                   GeneratorContext context) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

}
