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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.Property;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class WebappComponentGenerator implements ComponentGenerator<LogicalComponent<WebappImplementation>> {

    public WebappComponentGenerator(@Reference GeneratorRegistry registry) {
        registry.register(WebappImplementation.class, this);
    }

    @SuppressWarnings({"unchecked"})
    public void generate(LogicalComponent<WebappImplementation> component, GeneratorContext context) {
        ComponentDefinition<WebappImplementation> definition = component.getDefinition();
        ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>> componentType =
                definition.getImplementation().getComponentType();

        WebappPhysicalComponentDefinition pDefinition = new WebappPhysicalComponentDefinition();
        URI componentId = component.getUri();
        pDefinition.setComponentId(componentId);
        pDefinition.setGroupId(componentId.resolve("."));

        Map<String, Class<?>> referenceTypes = new HashMap<String, Class<?>>();
        for (ReferenceDefinition referenceDefinition : componentType.getReferences().values()) {
            String name = referenceDefinition.getName();
            Class<?> type = referenceDefinition.getServiceContract().getInterfaceClass();
            referenceTypes.put(name, type);
        }
        pDefinition.setReferenceTypes(referenceTypes);
        context.getPhysicalChangeSet().addComponentDefinition(pDefinition);
    }

    public WebAppPhysicalWireSourceDefinition generateWireSource(LogicalComponent<WebappImplementation> source,
                                                                 LogicalReference reference,
                                                                 boolean optimizable,
                                                                 GeneratorContext context) throws GenerationException {

        WebAppPhysicalWireSourceDefinition sourceDefinition = new WebAppPhysicalWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        return sourceDefinition;
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<WebappImplementation> target,
                                                           GeneratorContext context) throws GenerationException {
        throw new UnsupportedOperationException();
    }

}
