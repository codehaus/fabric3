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

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
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

    public PhysicalComponentDefinition generate(LogicalComponent<WebappImplementation> component) {
        ComponentDefinition<WebappImplementation> definition = component.getDefinition();
        ComponentType componentType = definition.getImplementation().getComponentType();

        URI componentId = component.getUri();

        WebappComponentDefinition physical = new WebappComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());

        Map<String, ReferenceDefinition> references = componentType.getReferences();
        Map<String, String> referenceTypes = new HashMap<String, String>(references.size());
        for (ReferenceDefinition referenceDefinition : references.values()) {
            String name = referenceDefinition.getName();
            ServiceContract<?> contract = referenceDefinition.getServiceContract();
            String interfaceClass = contract.getQualifiedInterfaceName();
            referenceTypes.put(name, interfaceClass);
        }
        physical.setReferenceTypes(referenceTypes);
        return physical;
    }

    public WebappWireSourceDefinition generateWireSource(LogicalComponent<WebappImplementation> source,
                                                         LogicalReference reference,
                                                         Policy policy) throws GenerationException {

        WebappWireSourceDefinition sourceDefinition = new WebappWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        return sourceDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<WebappImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service,
                                                           LogicalComponent<WebappImplementation> arg1,
                                                           Policy policy) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<WebappImplementation> source,
                                                                   LogicalResource<?> resource) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

}
