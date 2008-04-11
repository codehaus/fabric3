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
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDescription;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.InjectableAttribute;
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
import org.fabric3.web.introspection.WebComponentType;
import org.fabric3.web.introspection.WebImplementation;
import org.fabric3.web.provision.WebComponentDefinition;
import org.fabric3.web.provision.WebComponentWireSourceDefinition;
import org.fabric3.web.provision.WebConstants;
import org.fabric3.web.provision.WebContextInjectionSite;

/**
 * Generates commands to provision a web component.
 *
 * @version $Rev: 2931 $ $Date: 2008-02-28 04:49:35 -0800 (Thu, 28 Feb 2008) $
 */
@EagerInit
public class WebComponentGenerator implements ComponentGenerator<LogicalComponent<WebImplementation>> {

    public WebComponentGenerator(@Reference GeneratorRegistry registry) {
        registry.register(WebImplementation.class, this);
    }

    public PhysicalComponentDefinition generate(LogicalComponent<WebImplementation> component) {
        ComponentDefinition<WebImplementation> definition = component.getDefinition();
        WebComponentType componentType = definition.getImplementation().getComponentType();

        URI componentId = component.getUri();

        WebComponentDefinition physical = new WebComponentDefinition();
        physical.setComponentId(componentId);
        physical.setGroupId(component.getParent().getUri());
        Map<String, Map<String, InjectionSite>> sites = generateInjectionMapping(componentType);
        physical.setInjectionMappings(sites);
        URI classLoaderId = component.getParent().getUri();
        physical.setClassLoaderId(classLoaderId);
        URL archiveUrl = getWebXmlUrl(definition);
        physical.setWebArchiveUrl(archiveUrl);
        return physical;
    }

    public WebComponentWireSourceDefinition generateWireSource(LogicalComponent<WebImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {

        WebComponentWireSourceDefinition sourceDefinition = new WebComponentWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        return sourceDefinition;
    }

    public PhysicalWireSourceDefinition generateCallbackWireSource(LogicalComponent<WebImplementation> source,
                                                                   ServiceContract<?> serviceContract,
                                                                   Policy policy) throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public PhysicalWireTargetDefinition generateWireTarget(LogicalService service, LogicalComponent<WebImplementation> component, Policy policy)
            throws GenerationException {
        return null;
    }

    public PhysicalWireSourceDefinition generateResourceWireSource(LogicalComponent<WebImplementation> source, LogicalResource<?> resource)
            throws GenerationException {
        return null;
    }

    private Map<String, Map<String, InjectionSite>> generateInjectionMapping(WebComponentType type) {
        Map<String, Map<String, InjectionSite>> mappings = new HashMap<String, Map<String, InjectionSite>>();
        for (ReferenceDefinition definition : type.getReferences().values()) {
            generateReferenceInjectionMapping(definition, type, mappings);
        }
        for (Property property : type.getProperties().values()) {
            generatePropertyInjectionMapping(property, mappings);
        }
        return mappings;
    }

    private void generateReferenceInjectionMapping(ReferenceDefinition definition,
                                                   WebComponentType type,
                                                   Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(definition.getName());
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
            mappings.put(definition.getName(), mapping);
        }
        for (Map.Entry<String, Map<InjectionSite, InjectableAttribute>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, InjectableAttribute> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().getName().equals(definition.getName())) {
                    mapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }
        ServiceContract<?> contract = definition.getServiceContract();
        String interfaceClass = contract.getQualifiedInterfaceName();
        // inject the reference into the servlet context
        WebContextInjectionSite site = new WebContextInjectionSite(interfaceClass, WebContextInjectionSite.ContextType.SERVLET_CONTEXT);
        // TODO support conversation injection
        mapping.put(WebConstants.SERVLET_CONTEXT_SITE, site);
    }

    private void generatePropertyInjectionMapping(Property property, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(property.getName());
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
            mappings.put(property.getName(), mapping);
        }
        // inject the property into the servlet context
        // we don't need to do the type mappings from schema to Java so set Object as the type
        WebContextInjectionSite site = new WebContextInjectionSite(Object.class.getName(), WebContextInjectionSite.ContextType.SERVLET_CONTEXT);
        mapping.put(WebConstants.SERVLET_CONTEXT_SITE, site);
    }

    /**
     * Returns the URL for the web.xml descriptor for the component.
     *
     * @param definition the component definition
     * @return the web.xml URL.
     */
    private URL getWebXmlUrl(ComponentDefinition<WebImplementation> definition) {
        List<ResourceDescription<?>> descriptions = definition.getImplementation().getResourceDescriptions();
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
