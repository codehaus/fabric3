/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.web.control;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.ComponentContext;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import static org.fabric3.container.web.spi.WebApplicationActivator.CONTEXT_ATTRIBUTE;
import static org.fabric3.container.web.spi.WebApplicationActivator.OASIS_CONTEXT_ATTRIBUTE;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.java.InjectableAttribute;
import org.fabric3.model.type.java.InjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.contribution.ContributionUriEncoder;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;
import org.fabric3.web.introspection.WebComponentType;
import org.fabric3.web.introspection.WebImplementation;
import org.fabric3.web.provision.WebComponentDefinition;
import org.fabric3.web.provision.WebComponentWireSourceDefinition;
import static org.fabric3.web.provision.WebConstants.SERVLET_CONTEXT_SITE;
import static org.fabric3.web.provision.WebConstants.SESSION_CONTEXT_SITE;
import org.fabric3.web.provision.WebContextInjectionSite;
import static org.fabric3.web.provision.WebContextInjectionSite.ContextType.SERVLET_CONTEXT;
import static org.fabric3.web.provision.WebContextInjectionSite.ContextType.SESSION_CONTEXT;

/**
 * Generates commands to provision a web component.
 *
 * @version $Rev: 2931 $ $Date: 2008-02-28 04:49:35 -0800 (Thu, 28 Feb 2008) $
 */
@EagerInit
public class WebComponentGenerator implements ComponentGenerator<LogicalComponent<WebImplementation>> {
    private HostInfo info;
    private ContributionUriEncoder encoder;

    public WebComponentGenerator(@Reference HostInfo info, @Reference ContributionUriEncoder encoder) {
        this.info = info;
        this.encoder = encoder;
    }

    public PhysicalComponentDefinition generate(LogicalComponent<WebImplementation> component) throws GenerationException {
        ComponentDefinition<WebImplementation> definition = component.getDefinition();
        WebComponentType componentType = definition.getImplementation().getComponentType();
        URI componentId = component.getUri();
        // the context URL for the web application is derived from the component name relative to the domain
        String contextUrl = info.getDomain().relativize(componentId).toString();
        WebComponentDefinition physical = new WebComponentDefinition();
        physical.setContextUrl(contextUrl);
        Map<String, Map<String, InjectionSite>> sites = generateInjectionMapping(componentType);
        physical.setInjectionMappings(sites);
        processPropertyValues(component, physical);
        if (component.getZone() == null) {
            physical.setContributionUri(definition.getContributionUri());
        } else {
            URI encoded;
            try {
                encoded = encoder.encode(definition.getContributionUri());
            } catch (URISyntaxException e) {
                throw new GenerationException(e);
            }
            physical.setContributionUri(encoded);
        }
        return physical;
    }

    public WebComponentWireSourceDefinition generateWireSource(LogicalComponent<WebImplementation> source, LogicalReference reference, Policy policy)
            throws GenerationException {

        WebComponentWireSourceDefinition sourceDefinition = new WebComponentWireSourceDefinition();
        sourceDefinition.setUri(reference.getUri());
        if (reference.getDefinition().getServiceContract().isConversational()) {
            sourceDefinition.setInteractionType(InteractionType.CONVERSATIONAL);
        }
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
        generateContextInjectionMapping(type, mappings);
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
        // inject the reference into the session context
        WebContextInjectionSite site = new WebContextInjectionSite(interfaceClass, SESSION_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
        if (!contract.isConversational()) {
            // if the target service is non-conversational, also inject the reference into the servlet context
            WebContextInjectionSite servletContextsite = new WebContextInjectionSite(interfaceClass, SERVLET_CONTEXT);
            mapping.put(SERVLET_CONTEXT_SITE, servletContextsite);
        }
    }

    private void generatePropertyInjectionMapping(Property property, Map<String, Map<String, InjectionSite>> mappings) {
        Map<String, InjectionSite> mapping = mappings.get(property.getName());
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
            mappings.put(property.getName(), mapping);
        }
        // inject the property into the session context
        // we don't need to do the type mappings from schema to Java so set Object as the type
        WebContextInjectionSite site = new WebContextInjectionSite(Object.class.getName(), SERVLET_CONTEXT);
        mapping.put(SESSION_CONTEXT_SITE, site);
    }

    private void generateContextInjectionMapping(WebComponentType type, Map<String, Map<String, InjectionSite>> mappings) {
        // OASIS API
        Map<String, InjectionSite> oasisMapping = mappings.get(OASIS_CONTEXT_ATTRIBUTE);
        if (oasisMapping == null) {
            oasisMapping = new HashMap<String, InjectionSite>();
            WebContextInjectionSite site =
                    new WebContextInjectionSite(ComponentContext.class.getName(), SESSION_CONTEXT);
            oasisMapping.put(SESSION_CONTEXT_SITE, site);
            mappings.put(OASIS_CONTEXT_ATTRIBUTE, oasisMapping);
        }
        for (Map.Entry<String, Map<InjectionSite, InjectableAttribute>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, InjectableAttribute> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().equals(InjectableAttribute.OASIS_COMPONENT_CONTEXT)) {
                    oasisMapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }

        //OSOA API
        Map<String, InjectionSite> mapping = mappings.get(CONTEXT_ATTRIBUTE);
        if (mapping == null) {
            mapping = new HashMap<String, InjectionSite>();
            WebContextInjectionSite site =
                    new WebContextInjectionSite(ComponentContext.class.getName(), SESSION_CONTEXT);
            mapping.put(SESSION_CONTEXT_SITE, site);
            mappings.put(CONTEXT_ATTRIBUTE, mapping);
        }
        for (Map.Entry<String, Map<InjectionSite, InjectableAttribute>> entry : type.getInjectionSites().entrySet()) {
            for (Map.Entry<InjectionSite, InjectableAttribute> siteMap : entry.getValue().entrySet()) {
                if (siteMap.getValue().equals(InjectableAttribute.COMPONENT_CONTEXT)) {
                    mapping.put(entry.getKey(), siteMap.getKey());
                }
            }
        }


    }

    private void processPropertyValues(LogicalComponent<?> component, WebComponentDefinition physical) {
        for (Map.Entry<String, Document> entry : component.getPropertyValues().entrySet()) {
            String name = entry.getKey();
            Document value = entry.getValue();
            if (value != null) {
                physical.setPropertyValue(name, value);
            }
        }
    }


}
