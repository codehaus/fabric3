/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.model.logical;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.assembly.InvalidPropertyFileException;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.ResolutionException;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.domain.DomainService;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @version $Revision$ $Date$
 */
public class LogicalModelGeneratorImpl implements LogicalModelGenerator {

    private static final DocumentBuilderFactory DOCUMENT_FACTORY;
    private static final XPathFactory XPATH_FACTORY;
    
    private final WireResolver wireResolver;
    private final PromotionNormalizer promotionNormalizer;
    private final DomainService domainService;

    static {
        DOCUMENT_FACTORY = DocumentBuilderFactory.newInstance();
        DOCUMENT_FACTORY.setNamespaceAware(true);
        XPATH_FACTORY = XPathFactory.newInstance();
    }
    
    public LogicalModelGeneratorImpl(@Reference WireResolver wireResolver,
                                     @Reference PromotionNormalizer promotionNormalizer,
                                     @Reference DomainService domainService) {
        this.wireResolver = wireResolver;
        this.promotionNormalizer = promotionNormalizer;
        this.domainService = domainService;
    }

    @SuppressWarnings("unchecked")
    public List<LogicalComponent<?>> include(LogicalComponent<CompositeImplementation> parent, Composite composite)
            throws ActivateException {

        // merge the property values into the parent
        for (Property<?> property : composite.getProperties().values()) {
            String name = property.getName();
            if (parent.getPropertyValues().containsKey(name)) {
                throw new ActivateException("Duplicate property", name);
            }
            Document value = property.getDefaultValue();
            parent.setPropertyValue(name, value);
        }

        // instantiate all the components in the composite and add them to the parent
        String base = parent.getUri().toString();
        Collection<ComponentDefinition<? extends Implementation<?>>> definitions = composite.getComponents().values();
        List<LogicalComponent<?>> components = new ArrayList<LogicalComponent<?>>(definitions.size());

        for (ComponentDefinition<? extends Implementation<?>> definition : definitions) {
            LogicalComponent<?> logicalComponent = instantiate(parent, definition);
            // use autowire settings on the original composite as an override if they are not specified on the component
            Autowire autowire;
            if (definition.getAutowire() == Autowire.INHERITED) {
                autowire = composite.getAutowire();
            } else {
                autowire = definition.getAutowire();
            }
            if (autowire == Autowire.ON || autowire == Autowire.OFF) {
                logicalComponent.setAutowireOverride(autowire);
            }
            components.add(logicalComponent);
            parent.addComponent(logicalComponent);
        }

        List<LogicalService> services = new ArrayList<LogicalService>();
        // merge the composite service declarations into the parent
        for (CompositeService compositeService : composite.getServices().values()) {
            URI serviceURI = URI.create(base + '#' + compositeService.getName());
            URI promotedURI = compositeService.getPromote();
            LogicalService logicalService = new LogicalService(serviceURI, compositeService, parent);
            logicalService.setPromote(URI.create(base + "/" + promotedURI));
            for (BindingDefinition binding : compositeService.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }
            services.add(logicalService);
            parent.addService(logicalService);
        }

        // merge the composite reference definitions into the parent
        List<LogicalReference> references = new ArrayList<LogicalReference>(composite.getReferences().size());
        for (CompositeReference compositeReference : composite.getReferences().values()) {
            URI referenceURi = URI.create(base + '#' + compositeReference.getName());
            LogicalReference logicalReference = new LogicalReference(referenceURi, compositeReference, parent);
            for (URI promotedUri : compositeReference.getPromoted()) {
                URI resolvedUri = URI.create(base + "/" + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }
            for (BindingDefinition binding : compositeReference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            references.add(logicalReference);
            parent.addReference(logicalReference);
        }

        // resolve wires for composite services merged into the domain
        try {
            for (LogicalService service : services) {
                wireResolver.resolve(service);
            }
        } catch (ResolutionException e) {
            throw new ActivateException(e);
        }

        // resove composite references merged into the domain
        for (LogicalReference reference : references) {
            try {
                wireResolver.resolveReference(reference, domainService.getDomain());
            } catch (ResolutionException e) {
                throw new ActivateException(e);
            }
        }

        // resolve wires for each new component
        try {
            for (LogicalComponent<?> component : components) {
                wireResolver.resolve(component);
            }
        } catch (ResolutionException e) {
            throw new ActivateException(e);
        }

        // normalize bindings for each new component
        for (LogicalComponent<?> component : components) {
            normalize(component);
        }

        return components;

    }

    /**
     * Instantiates a logical component from a component definition
     *
     * @param parent     the parent logical component
     * @param definition the component definition to instantiate from
     * @return the instantiated logical component
     * @throws InstantiationException if an error occurs during instantiation
     */
    public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalComponent<CompositeImplementation> parent,
                                                                         ComponentDefinition<I> definition) throws InstantiationException {
        
        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        
        I impl = definition.getImplementation();
        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(impl.getType())) {
            return instantiateComposite(parent, definition, uri);
        } else {
            return instantiateAtomicComponent(parent, definition, uri);
        }
        
    }

    private <I extends Implementation<?>> LogicalComponent<I> instantiateAtomicComponent(
            LogicalComponent<CompositeImplementation> parent,
            ComponentDefinition<I> definition,
            URI uri) throws InstantiationException {
        URI runtimeId = definition.getRuntimeId();
        LogicalComponent<I> component = new LogicalComponent<I>(uri, runtimeId, definition, parent);
        initializeProperties(component, definition);
        // this is an atomic component so create and bind its services, references and resources
        I impl = definition.getImplementation();
        AbstractComponentType<?, ?, ?, ?> componentType = impl.getComponentType();

        for (ServiceDefinition service : componentType.getServices().values()) {
            String name = service.getName();
            URI serviceUri = uri.resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                // service is configured in the component definition
                for (BindingDefinition binding : componentService.getBindings()) {
                    logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
                }
            }
            component.addService(logicalService);
        }

        for (ReferenceDefinition reference : componentType.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = uri.resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);
            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                // reference is configured
                for (BindingDefinition binding : componentReference.getBindings()) {
                    logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
                }
            }
            component.addReference(logicalReference);
        }

        for (ResourceDefinition resource : componentType.getResources().values()) {
            URI resourceUri = uri.resolve('#' + resource.getName());
            LogicalResource<?> logicalResource = createLogicalResource(resource, resourceUri, component);
            component.addResource(logicalResource);
        }
        return component;

    }

    private <I extends Implementation<?>> LogicalComponent<I> instantiateComposite(
            LogicalComponent<CompositeImplementation> parent,
            ComponentDefinition<I> definition,
            URI uri) throws InstantiationException {
        // this component is implemented by a composite so we need to create its children
        // and promote services and references
        URI runtimeId = definition.getRuntimeId();
        LogicalComponent<I> component = new LogicalComponent<I>(uri, runtimeId, definition, parent);
        initializeProperties(component, definition);

        @SuppressWarnings({"unchecked"})
        LogicalComponent<CompositeImplementation> compositeComponent =
                (LogicalComponent<CompositeImplementation>) component;
        Composite composite = compositeComponent.getDefinition().getImplementation().getComponentType();

        // create the child components
        for (ComponentDefinition<? extends Implementation<?>> child : composite.getComponents().values()) {
            component.addComponent(instantiate(compositeComponent, child));
        }
        instantiateCompositeServices(uri, component, composite);
        instantiateCompositeReferences(parent, uri, component, composite);


        return component;
    }

    private <I extends Implementation<?>> void instantiateCompositeServices(URI uri,
                                                                            LogicalComponent<I> component,
                                                                            Composite composite) {
        ComponentDefinition<I> definition = component.getDefinition();
        for (CompositeService service : composite.getServices().values()) {
            String name = service.getName();
            URI serviceUri = uri.resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            if (service.getPromote() != null) {
                logicalService.setPromote(URI.create(uri.toString() + "/" + service.getPromote()));
            }
            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }
            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                // Merge/override logical reference configuration created above with service configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // service configuration. This information must be merged with or used to override any
                // configuration that was created by service promotions within the composite
                if (!componentService.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentService.getBindings()) {
                        bindings.add(new LogicalBinding<BindingDefinition>(binding, logicalService));
                    }
                    logicalService.overrideBindings(bindings);
                }
            }
            component.addService(logicalService);
        }
    }

    private <I extends Implementation<?>> void instantiateCompositeReferences(
            LogicalComponent<CompositeImplementation> parent,
            URI uri,
            LogicalComponent<I> component,
            Composite composite) {
        ComponentDefinition<I> definition = component.getDefinition();
        // create logical references based on promoted references in the composite definition
        for (CompositeReference reference : composite.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = uri.resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);
            for (BindingDefinition binding : reference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            for (URI promotedUri : reference.getPromoted()) {
                URI resolvedUri = URI.create(uri.toString() + "/" + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }
            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                // Merge/override logical reference configuration created above with reference configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // reference configuration. This information must be merged with or used to override any
                // configuration that was created by reference promotions within the composite
                if (!componentReference.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentReference.getBindings()) {
                        bindings.add(new LogicalBinding<BindingDefinition>(binding, logicalReference));
                    }
                    logicalReference.overrideBindings(bindings);
                }
                if (!componentReference.getTargets().isEmpty()) {
                    List<URI> targets = new ArrayList<URI>();
                    for (URI targetUri : componentReference.getTargets()) {
                        // the target is relative to the component's parent, not the component
                        targets.add(URI.create(parent.getUri().toString() + "/" + targetUri));
                    }
                    logicalReference.overrideTargets(targets);
                }
            }
            component.addReference(logicalReference);
        }
    }

    /**
     * Set the initial actual property values of a component.
     *
     * @param component  the component to initialize
     * @param definition the definition of the component
     * @throws InstantiationException if there was a problem initializing a property value
     */
    private <I extends Implementation<?>> void initializeProperties(LogicalComponent<I> component,
                                                                      ComponentDefinition<I> definition)
            throws InstantiationException {
        Map<String, PropertyValue> propertyValues = definition.getPropertyValues();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getComponentType();
        for (Property<?> property : componentType.getProperties().values()) {
            String name = property.getName();
            PropertyValue propertyValue = propertyValues.get(name);
            Document value;
            if (propertyValue == null) {
                // use default value from component type
                value = property.getDefaultValue();
            } else {
                // the spec defines the following sequence
                if (propertyValue.getFile() != null) {
                    // load the value from an external resource
                    value = loadValueFromFile(property.getName(), propertyValue.getFile());
                } else if (propertyValue.getSource() != null) {
                    // get the value by evaluating an XPath against the composite properties
                    try {
                        value = deriveValueFromXPath(propertyValue.getSource(), component.getParent());
                    } catch (XPathExpressionException e) {
                        throw new InstantiationException(e.getMessage(), name, e);
                    }
                } else {
                    // use inline XML file
                    value = propertyValue.getValue();
                }

            }
            component.setPropertyValue(name, value);
        }
    }

    private Document loadValueFromFile(String name, URI file) throws InvalidPropertyFileException {
        DocumentBuilder builder;
        try {
            builder = DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }

        URL resource;
        try {
            resource = file.toURL();
        } catch (MalformedURLException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        }

        InputStream inputStream;
        try {
            inputStream = resource.openStream();
        } catch (IOException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        }

        try {
            return builder.parse(inputStream);
        } catch (IOException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        } catch (SAXException e) {
            throw new InvalidPropertyFileException(e.getMessage(), name, e, file);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public Document deriveValueFromXPath(String source, final LogicalComponent<?> parent)
            throws XPathExpressionException {
        XPathVariableResolver variableResolver = new XPathVariableResolver() {
            public Object resolveVariable(QName qName) {
                String name = qName.getLocalPart();
                Document value = parent.getPropertyValue(name);
                if (value == null) {
                    return null;
                }
                return value.getDocumentElement();
            }
        };
        XPath xpath = XPATH_FACTORY.newXPath();
        xpath.setXPathVariableResolver(variableResolver);

        DocumentBuilder builder;
        try {
            builder = DOCUMENT_FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }

        Document value = builder.newDocument();
        Element root = value.createElement("value");
        // TODO do we need to copy namespace declarations to this root
        value.appendChild(root);
        try {
            NodeList result = (NodeList) xpath.evaluate(source, root, XPathConstants.NODESET);
            for (int i = 0; i < result.getLength(); i++) {
                Node node = result.item(i);
                value.adoptNode(node);
                root.appendChild(node);
            }
        } catch (XPathExpressionException e) {
            // FIXME rethrow this for now, fix if people find it confusing
            // the Apache and Sun implementations of XPath throw a nested NullPointerException
            // if the xpath contains an unresolvable variable. It might be better to throw
            // a more descriptive cause, but that also might be confusing for people who
            // are used to this behaviour
            throw e;
        }
        return value;
    }

    /**
     * Normalizes the component and any children
     *
     * @param component the component to normalize
     */
    private void normalize(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(implementation.getType())) {
            for (LogicalComponent<?> child : component.getComponents()) {
                normalize(child);
            }
        } else {
            promotionNormalizer.normalize(component);
        }
    }

    /*
     * Creates a logical resource.
     */
    private <RD extends ResourceDefinition> LogicalResource<RD> createLogicalResource(RD resourceDefinition,
                                                                              URI resourceUri,
                                                                              LogicalComponent<?> component) {
        return new LogicalResource<RD>(resourceUri, resourceDefinition, component);
    }

}
