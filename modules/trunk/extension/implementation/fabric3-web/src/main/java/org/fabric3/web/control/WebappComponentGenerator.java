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
package org.fabric3.web.control;

import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ResourceDescription;
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
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.policy.Policy;
import org.fabric3.web.introspection.WebappImplementation;
import org.fabric3.web.provision.WebappWireSourceDefinition;
import org.fabric3.web.provision.WebappComponentDefinition;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Generates commands to provision a web component.
 *
 * @version $Rev: 2931 $ $Date: 2008-02-28 04:49:35 -0800 (Thu, 28 Feb 2008) $
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

        Map<String, String> referenceTypes = generateReferenceTypes(componentType);
        physical.setReferenceTypes(referenceTypes);
        URI classLoaderId = component.getParent().getUri();
        physical.setClassLoaderId(classLoaderId);
        URL archiveUrl = getArchiveUrl(definition.getImplementation().getResourceDescriptions());
        physical.setWebArchiveUrl(archiveUrl);
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
        return null;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<WebappImplementation> source,
                                                                   LogicalResource<?> resource) throws GenerationException {
        return null;
    }

    private Map<String, String> generateReferenceTypes(ComponentType componentType) {
        Map<String, ReferenceDefinition> references = componentType.getReferences();
        Map<String, String> referenceTypes = new HashMap<String, String>(references.size());
        for (ReferenceDefinition referenceDefinition : references.values()) {
            String name = referenceDefinition.getName();
            ServiceContract<?> contract = referenceDefinition.getServiceContract();
            String interfaceClass = contract.getQualifiedInterfaceName();
            referenceTypes.put(name, interfaceClass);
        }
        return referenceTypes;
    }

    private URL getArchiveUrl(List<ResourceDescription<?>> descriptions) {
        for (ResourceDescription<?> description : descriptions) {
            if (description instanceof ContributionResourceDescription) {
                ContributionResourceDescription contribDesc = (ContributionResourceDescription) description;
                if (contribDesc.getArtifactUrls().isEmpty()) {
                    return null;
                }
                // getting the first URL is ok since WAR files are self-contained
                return contribDesc.getArtifactUrls().get(0);
            }
        }
        throw new AssertionError("WAR URL not found");
    }


}
