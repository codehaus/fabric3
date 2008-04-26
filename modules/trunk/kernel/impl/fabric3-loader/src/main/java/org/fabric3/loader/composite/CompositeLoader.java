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
package org.fabric3.loader.composite;

import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Constructor;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidServiceException;
import org.fabric3.introspection.DefaultIntrospectionContext;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Include;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.Property;
import org.fabric3.scdl.WireDefinition;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderRegistry;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.Loader;

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
     * @param loaderHelper    helper
     */
    public CompositeLoader(Loader loader,
                           TypeLoader<Include> includeLoader,
                           TypeLoader<Property> propertyLoader,
                           TypeLoader<ComponentDefinition<?>> componentLoader,
                           LoaderHelper loaderHelper) {
        this.loader = loader;
        this.includeLoader = includeLoader;
        this.propertyLoader = propertyLoader;
        this.componentLoader = componentLoader;
        this.loaderHelper = loaderHelper;

        this.registry = null;
        this.serviceLoader = null;
        this.referenceLoader = null;
        this.wireLoader = null;
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
                           @Reference(name = "include")TypeLoader<Include> includeLoader,
                           @Reference(name = "property")TypeLoader<Property> propertyLoader,
                           @Reference(name = "service")TypeLoader<CompositeService> serviceLoader,
                           @Reference(name = "reference")TypeLoader<CompositeReference> referenceLoader,
                           @Reference(name = "component")TypeLoader<ComponentDefinition<?>> componentLoader,
                           @Reference(name = "wire")TypeLoader<WireDefinition> wireLoader,
                           @Reference(name = "loaderHelper")LoaderHelper loaderHelper) {
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

    public Composite load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
        boolean local = Boolean.valueOf(reader.getAttributeValue(null, "local"));
        introspectionContext = new DefaultIntrospectionContext(introspectionContext, targetNamespace);
        QName compositeName = new QName(targetNamespace, name);
        QName constrainingType = LoaderUtil.getQName(reader.getAttributeValue(null, "constrainingType"),
                                                     targetNamespace,
                                                     reader.getNamespaceContext());

        Composite type = new Composite(compositeName);
        type.setLocal(local);
        type.setAutowire(Autowire.fromString(reader.getAttributeValue(null, "autowire")));
        type.setConstrainingType(constrainingType);
        loaderHelper.loadPolicySetsAndIntents(type, reader);

        try {
            while (true) {
                switch (reader.next()) {
                case START_ELEMENT:
                    QName qname = reader.getName();
                    if (INCLUDE.equals(qname)) {
                        Include include = includeLoader.load(reader, introspectionContext);
                        QName includeName = include.getName();
                        if (type.getIncludes().containsKey(includeName)) {
                            String identifier = includeName.toString();
                            throw new DuplicateIncludeException("Include already defined with name :" + identifier, identifier);
                        }
                        for (ComponentDefinition definition : include.getIncluded().getComponents().values()) {
                            String key = definition.getName();
                            if (type.getComponents().containsKey(key)) {
                                throw new DuplicateComponentNameException("Component with name already defined: " + key, key);
                            }
                        }
                        type.add(include);
                    } else if (PROPERTY.equals(qname)) {
                        Property property = propertyLoader.load(reader, introspectionContext);
                        String key = property.getName();
                        if (type.getProperties().containsKey(key)) {
                            throw new DuplicatePropertyException("Property already defined: " + key, key);
                        }
                        type.add(property);
                    } else if (SERVICE.equals(qname)) {
                        CompositeService service = serviceLoader.load(reader, introspectionContext);
                        if (type.getServices().containsKey(service.getName())) {
                            String key = service.getName();
                            throw new DuplicateServiceException("Service already defined: " + key, key);
                        }
                        type.add(service);
                    } else if (REFERENCE.equals(qname)) {
                        CompositeReference reference = referenceLoader.load(reader, introspectionContext);
                        if (type.getReferences().containsKey(reference.getName())) {
                            String key = reference.getName();
                            throw new DuplicateReferenceException("Reference already defined: " + key, key);
                        }
                        type.add(reference);
                    } else if (COMPONENT.equals(qname)) {
                        ComponentDefinition<?> componentDefinition = componentLoader.load(reader, introspectionContext);
                        String key = componentDefinition.getName();
                        if (type.getComponents().containsKey(key)) {
                            throw new DuplicateComponentNameException("Component with name already defined: " + key, key);
                        }
                        if (type.getAutowire() != Autowire.INHERITED && componentDefinition.getAutowire() == Autowire.INHERITED) {
                            componentDefinition.setAutowire(type.getAutowire());
                        }
                        type.add(componentDefinition);
                    } else if (WIRE.equals(qname)) {
                        WireDefinition wire = wireLoader.load(reader, introspectionContext);
                        type.add(wire);
                    } else {
                        // Extension element - for now try to load and see if we can handle it
                        ModelObject modelObject = loader.load(reader, ModelObject.class, introspectionContext);
                        if (modelObject instanceof Property) {
                            type.add((Property) modelObject);
                        } else if (modelObject instanceof CompositeService) {
                            type.add((CompositeService) modelObject);
                        } else if (modelObject instanceof CompositeReference) {
                            type.add((CompositeReference) modelObject);
                        } else if (modelObject instanceof ComponentDefinition) {
                            type.add((ComponentDefinition<?>) modelObject);
                        } else {
                            // Unknown extension element, ignore
                        }
                    }
                    break;
                case END_ELEMENT:
                    assert COMPOSITE.equals(reader.getName());
                    return type;
                }
            }
        } catch (CompositeLoaderException e) {
            e.setCompositeName(compositeName);
            throw e;
        }
    }
}
