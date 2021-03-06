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
 * --- Original Apache License ---
 *
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
package org.fabric3.loader.composite;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.contribution.ArtifactValidationFailure;
import org.fabric3.model.type.ModelObject;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Include;
import org.fabric3.model.type.component.Property;
import org.fabric3.model.type.component.WireDefinition;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.Loader;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderRegistry;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.introspection.xml.UnrecognizedElement;
import org.fabric3.spi.introspection.xml.UnrecognizedElementException;
import org.fabric3.spi.util.UriHelper;

/**
 * Loads a composite component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class CompositeLoader implements TypeLoader<Composite> {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    public static final QName INCLUDE = new QName(SCA_NS, "include");
    public static final QName PROPERTY = new QName(SCA_NS, "property");
    public static final QName SERVICE = new QName(SCA_NS, "service");
    public static final QName REFERENCE = new QName(SCA_NS, "reference");
    public static final QName COMPONENT = new QName(SCA_NS, "component");
    public static final QName WIRE = new QName(SCA_NS, "wire");

    private static final Map<String, String> ATTRIBUTES = new HashMap<String, String>();

    static {
        ATTRIBUTES.put("name", "name");
        ATTRIBUTES.put("autowire", "autowire");
        ATTRIBUTES.put("targetNamespace", "targetNamespace");
        ATTRIBUTES.put("local", "local");
        ATTRIBUTES.put("requires", "requires");
        ATTRIBUTES.put("policySets", "policySets");
        ATTRIBUTES.put("constrainingType", "constrainingType");
    }

    private final LoaderRegistry registry;
    private final Loader loader;
    private final TypeLoader<Include> includeLoader;
    private final TypeLoader<Property> propertyLoader;
    private final TypeLoader<CompositeService> serviceLoader;
    private final TypeLoader<CompositeReference> referenceLoader;
    private final TypeLoader<ComponentDefinition<?>> componentLoader;
    private final TypeLoader<WireDefinition> wireLoader;
    private final LoaderHelper loaderHelper;

    /**
     * Constructor used during bootstrap.
     *
     * @param loader          loader for extension elements
     * @param includeLoader   loader for include elements
     * @param propertyLoader  loader for composite property elements
     * @param componentLoader loader for component elements
     * @param wireLoader      loader for wire elements
     * @param loaderHelper    helper
     */
    public CompositeLoader(Loader loader,
                           TypeLoader<Include> includeLoader,
                           TypeLoader<Property> propertyLoader,
                           TypeLoader<ComponentDefinition<?>> componentLoader,
                           TypeLoader<WireDefinition> wireLoader,
                           LoaderHelper loaderHelper) {
        this.loader = loader;
        this.includeLoader = includeLoader;
        this.propertyLoader = propertyLoader;
        this.componentLoader = componentLoader;
        this.wireLoader = wireLoader;
        this.loaderHelper = loaderHelper;

        this.registry = null;
        this.serviceLoader = null;
        this.referenceLoader = null;
    }

    /**
     * Constructor to be used when registering this component through SCDL.
     *
     * @param registry        the loader registry to register with; also used to load extension elements
     * @param includeLoader   loader for include elements
     * @param propertyLoader  loader for composite property elements
     * @param serviceLoader   loader for composite services
     * @param referenceLoader loader for composite references
     * @param componentLoader loader for component elements
     * @param wireLoader      loader for wire elements
     * @param loaderHelper    helper
     */
    @Constructor
    public CompositeLoader(@Reference LoaderRegistry registry,
                           @Reference(name = "include") TypeLoader<Include> includeLoader,
                           @Reference(name = "property") TypeLoader<Property> propertyLoader,
                           @Reference(name = "service") TypeLoader<CompositeService> serviceLoader,
                           @Reference(name = "reference") TypeLoader<CompositeReference> referenceLoader,
                           @Reference(name = "component") TypeLoader<ComponentDefinition<?>> componentLoader,
                           @Reference(name = "wire") TypeLoader<WireDefinition> wireLoader,
                           @Reference(name = "loaderHelper") LoaderHelper loaderHelper) {
        this.registry = registry;
        this.loader = registry;
        this.includeLoader = includeLoader;
        this.propertyLoader = propertyLoader;
        this.serviceLoader = serviceLoader;
        this.referenceLoader = referenceLoader;
        this.componentLoader = componentLoader;
        this.wireLoader = wireLoader;
        this.loaderHelper = loaderHelper;
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    @Init
    public void init() {
        registry.registerLoader(COMPOSITE, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(COMPOSITE);
    }

    public Composite load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        validateAttributes(reader, introspectionContext);
        String name = reader.getAttributeValue(null, "name");
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        boolean local = Boolean.valueOf(reader.getAttributeValue(null, "local"));
        IntrospectionContext childContext = new DefaultIntrospectionContext(introspectionContext, targetNamespace);
        QName compositeName = new QName(targetNamespace, name);
        NamespaceContext namespace = reader.getNamespaceContext();
        String constrainingTypeAttrbute = reader.getAttributeValue(null, "constrainingType");
        QName constrainingType = LoaderUtil.getQName(constrainingTypeAttrbute, targetNamespace, namespace);

        Composite type = new Composite(compositeName);
        type.setLocal(local);
        type.setAutowire(Autowire.fromString(reader.getAttributeValue(null, "autowire")));
        type.setConstrainingType(constrainingType);
        loaderHelper.loadPolicySetsAndIntents(type, reader, childContext);
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                QName qname = reader.getName();
                if (INCLUDE.equals(qname)) {
                    handleInclude(type, reader, childContext);
                    continue;
                } else if (PROPERTY.equals(qname)) {
                    handleProperty(type, reader, childContext);
                    continue;
                } else if (SERVICE.equals(qname)) {
                    handleService(type, reader, childContext);
                    continue;
                } else if (REFERENCE.equals(qname)) {
                    handleReference(type, reader, childContext);
                    continue;
                } else if (COMPONENT.equals(qname)) {
                    handleComponent(type, reader, childContext);
                    continue;
                } else if (WIRE.equals(qname)) {
                    handleWire(type, reader, childContext);
                    continue;
                } else {
                    handleExtensionElement(type, reader, childContext);
                    continue;
                }
            case END_ELEMENT:
                assert COMPOSITE.equals(reader.getName());
                validateServicePromotions(type, reader, childContext);
                validateReferencePromotions(type, reader, childContext);
                if (childContext.hasErrors() || childContext.hasWarnings()) {
                    URI uri = introspectionContext.getContributionUri();
                    if (childContext.hasErrors()) {
                        ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                        artifactFailure.addFailures(childContext.getErrors());
                        introspectionContext.addError(artifactFailure);
                    }
                    if (childContext.hasWarnings()) {
                        ArtifactValidationFailure artifactFailure = new ArtifactValidationFailure(uri, compositeName.toString());
                        artifactFailure.addFailures(childContext.getWarnings());
                        introspectionContext.addWarning(artifactFailure);
                    }
                }
                return type;
            }
        }
    }

    private void handleExtensionElement(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        // Extension element - for now try to load and see if we can handle it
        ModelObject modelObject;
        try {
            modelObject = loader.load(reader, ModelObject.class, childContext);
            // TODO when the loader registry is replaced this try..catch must be replaced with a check for a loader and an
            // UnrecognizedElement added to the context if none is found
        } catch (UnrecognizedElementException e) {
            UnrecognizedElement failure = new UnrecognizedElement(reader);
            childContext.addError(failure);
            return;
        }
        if (modelObject instanceof Property) {
            type.add((Property) modelObject);
        } else if (modelObject instanceof CompositeService) {
            type.add((CompositeService) modelObject);
        } else if (modelObject instanceof CompositeReference) {
            type.add((CompositeReference) modelObject);
        } else if (modelObject instanceof ComponentDefinition) {
            type.add((ComponentDefinition<?>) modelObject);
        } else if (type == null) {
            // there was an error loading the element, ingore it as the errors will have been reported
        } else {
            childContext.addError(new UnrecognizedElement(reader));
        }
    }

    private void handleWire(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        WireDefinition wire = wireLoader.load(reader, childContext);
        if (wire == null) {
            // errror encountered loading the wire
            return;
        }
        type.add(wire);
    }

    private void handleComponent(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        ComponentDefinition<?> componentDefinition = componentLoader.load(reader, childContext);
        if (componentDefinition == null) {
            // errror encountered loading the componentDefinition
            return;
        }
        String key = componentDefinition.getName();
        if (type.getComponents().containsKey(key)) {
            DuplicateComponentName failure = new DuplicateComponentName(key, reader);
            childContext.addError(failure);
            return;
        }
        if (type.getAutowire() != Autowire.INHERITED && componentDefinition.getAutowire() == Autowire.INHERITED) {
            componentDefinition.setAutowire(type.getAutowire());
        }
        type.add(componentDefinition);
    }

    private void handleReference(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        CompositeReference reference = referenceLoader.load(reader, childContext);
        if (reference == null) {
            // errror encountered loading the reference
            return;
        }
        if (type.getReferences().containsKey(reference.getName())) {
            String key = reference.getName();
            DuplicatePromotedReferenceName failure = new DuplicatePromotedReferenceName(key, reader);
            childContext.addError(failure);
        } else {
            type.add(reference);
        }
    }

    private void handleService(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        CompositeService service = serviceLoader.load(reader, childContext);
        if (service == null) {
            // errror encountered loading the service
            return;
        }
        if (type.getServices().containsKey(service.getName())) {
            String key = service.getName();
            DuplicateService failure = new DuplicateService(key, reader);
            childContext.addError(failure);
        } else {
            type.add(service);
        }
    }

    private void handleProperty(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        Property property = propertyLoader.load(reader, childContext);
        if (property == null) {
            // errror encountered loading the property
            return;
        }
        String key = property.getName();
        if (type.getProperties().containsKey(key)) {
            DuplicateProperty failure = new DuplicateProperty(key, reader);
            childContext.addError(failure);
        } else {
            type.add(property);
        }
    }

    private void handleInclude(Composite type, XMLStreamReader reader, IntrospectionContext childContext) throws XMLStreamException {
        Include include = includeLoader.load(reader, childContext);
        if (include == null) {
            // errror encountered loading the include
            return;
        }
        QName includeName = include.getName();
        if (type.getIncludes().containsKey(includeName)) {
            String identifier = includeName.toString();
            DuplicateInclude failure = new DuplicateInclude(identifier, reader);
            childContext.addError(failure);
            return;
        }
        for (ComponentDefinition definition : include.getIncluded().getComponents().values()) {
            String key = definition.getName();
            if (type.getComponents().containsKey(key)) {
                DuplicateComponentName failure = new DuplicateComponentName(key, reader);
                childContext.addError(failure);
            }
        }
        type.add(include);
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!ATTRIBUTES.containsKey(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    private void validateServicePromotions(Composite type, XMLStreamReader reader, IntrospectionContext childContext) {
        for (CompositeService service : type.getServices().values()) {
            URI promotedUri = service.getPromote();
            String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
            ComponentDefinition promoted = type.getComponents().get(componentName);
            if (promoted == null) {
                PromotionNotFound error =
                        new PromotionNotFound("Component " + componentName + " referenced by " + service.getName() + " not found", reader);
                childContext.addError(error);
            } else {
                String serviceName = promotedUri.getFragment();
                if (serviceName == null && promoted.getComponentType().getServices().size() != 1) {
                    PromotionNotFound error =
                            new PromotionNotFound("A promoted service must be specified for " + service.getName(), reader);
                    childContext.addError(error);
                }
                if (serviceName != null && !promoted.getComponentType().getServices().containsKey(serviceName)) {
                    PromotionNotFound error =
                            new PromotionNotFound("Service " + serviceName + " promoted by " + service.getName() + " not found", reader);
                    childContext.addError(error);
                }
            }
        }
    }

    private void validateReferencePromotions(Composite type, XMLStreamReader reader, IntrospectionContext childContext) {
        for (CompositeReference reference : type.getReferences().values()) {
            for (URI promotedUri : reference.getPromotedUris()) {
                String componentName = UriHelper.getDefragmentedNameAsString(promotedUri);
                ComponentDefinition promoted = type.getComponents().get(componentName);
                if (promoted == null) {
                    PromotionNotFound error =
                            new PromotionNotFound("Component " + componentName + " referenced by " + reference.getName() + " not found", reader);
                    childContext.addError(error);
                } else {
                    String referenceName = promotedUri.getFragment();
                    if (referenceName == null && promoted.getComponentType().getReferences().size() != 1) {
                        PromotionNotFound error =
                                new PromotionNotFound("A promoted reference must be specified for " + reference.getName(), reader);
                        childContext.addError(error);
                    }
                    if (referenceName != null && !promoted.getComponentType().getReferences().containsKey(referenceName)) {
                        PromotionNotFound error =
                                new PromotionNotFound("Reference " + referenceName + " promoted by " + reference.getName() + " not found", reader);
                        childContext.addError(error);
                    }
                }
            }
        }
    }


}
