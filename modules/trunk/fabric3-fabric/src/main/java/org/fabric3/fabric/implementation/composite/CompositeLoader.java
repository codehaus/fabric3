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
package org.fabric3.fabric.implementation.composite;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.Reference;

import org.fabric3.extension.loader.LoaderExtension;
import org.fabric3.spi.deployer.CompositeClassLoader;
import org.fabric3.spi.loader.InvalidConfigurationException;
import org.fabric3.spi.loader.InvalidServiceException;
import org.fabric3.spi.loader.InvalidWireException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.Include;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ReferenceTarget;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.model.type.WireDefinition;
import org.fabric3.spi.services.artifact.Artifact;
import org.fabric3.spi.services.artifact.ArtifactRepository;
import org.fabric3.spi.util.stax.StaxUtil;

/**
 * Loads a composite component definition from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class CompositeLoader extends LoaderExtension<CompositeComponentType> {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    public static final String URI_DELIMITER = "/";

    private final ArtifactRepository artifactRepository;

    public CompositeLoader(@Reference LoaderRegistry registry, @Reference ArtifactRepository artifactRepository) {
        super(registry);
        this.artifactRepository = artifactRepository;
    }

    public QName getXMLType() {
        return COMPOSITE;
    }

    public CompositeComponentType load(XMLStreamReader reader, LoaderContext loaderContext)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        String autowire = reader.getAttributeValue(null, "autowire");
        QName compositeName = StaxUtil.createQName(name, reader);
        CompositeComponentType type = new CompositeComponentType(compositeName);

        if ("true".equalsIgnoreCase(autowire)) {
            type.setAutowire(Autowire.ON);
        } else if (autowire == null) {
            type.setAutowire(Autowire.INHERITED);
        } else {
            type.setAutowire(Autowire.OFF);
        }
        while (true) {
            switch (reader.next()) {
            case START_ELEMENT:
                ModelObject loadedType = registry.load(reader, loaderContext);
                if (loadedType instanceof ServiceDefinition) {
                    type.add((ServiceDefinition) loadedType);
                } else if (loadedType instanceof ReferenceDefinition) {
                    type.add((ReferenceDefinition) loadedType);
                } else if (loadedType instanceof Property<?>) {
                    type.add((Property<?>) loadedType);
                } else if (loadedType instanceof ComponentDefinition<?>) {
                    type.add((ComponentDefinition<?>) loadedType);
                } else if (loadedType instanceof Include) {
                    Include include = (Include) loadedType;
                    String includeName = include.getName();
                    if (type.getIncludes().containsKey(includeName)) {
                        throw new DuplicateIncludeException("Include already defined with name", includeName);
                    }
                    type.add(include);
                } else if (loadedType instanceof Dependency) {
                    Artifact artifact = ((Dependency) loadedType).getArtifact();
                    if (artifactRepository == null) {
                        throw new MissingResourceException("No ArtifactRepository configured for this system",
                                                           artifact.toString()
                        );
                    }

                    // default to jar type if not specified
                    if (artifact.getType() == null) {
                        artifact.setType("jar");
                    }
                    artifactRepository.resolve(artifact);
                    if (artifact.getUrl() == null) {
                        throw new MissingResourceException("Dependency not found", artifact.toString());
                    }

                    ClassLoader classLoader = loaderContext.getClassLoader();
                    if (classLoader instanceof CompositeClassLoader) {
                        CompositeClassLoader ccl = (CompositeClassLoader) classLoader;
                        for (URL dep : artifact.getUrls()) {
                            ccl.addURL(dep);
                        }
                    }
                } else if (loadedType instanceof WireDefinition) {
                    type.add((WireDefinition) loadedType);
                } else if (loadedType != null) {
                    throw new InvalidConfigurationException("Invalid element type", loadedType.getClass().getName());
                }
                reader.next();
                break;
            case END_ELEMENT:
                assert COMPOSITE.equals(reader.getName());
                // if there are wire defintions then link them up to the relevant components
                resolveWires(type);
                verifyCompositeCompleteness(type);
                return type;
            }
        }
    }

    protected void resolveWires(CompositeComponentType composite) throws InvalidWireException {
        ComponentDefinition componentDefinition;
        ServiceDefinition serviceDefinition;
        List<WireDefinition> wireDefns = composite.getDeclaredWires();
        for (WireDefinition wire : wireDefns) {
            URI targetUri = wire.getTarget();
            // validate the target before finding the source
            validateTarget(targetUri, composite);

            String sourceName = wire.getSource().getPath(); //new QualifiedName(wire.getSource().getPath());
            serviceDefinition = composite.getDeclaredServices().get(sourceName);
            if (serviceDefinition != null) {
                serviceDefinition.setTarget(wire.getTarget());
            } else {
                componentDefinition = composite.getDeclaredComponents().get(sourceName);
                if (componentDefinition != null) {
                    if (wire.getSource().getFragment() == null) {
                        throw new InvalidWireException("Source reference not specified", sourceName);
                    }
                    URI referenceName = URI.create(wire.getSource().getFragment());
                    ReferenceTarget referenceTarget = createReferenceTarget(referenceName,
                                                                            targetUri,
                                                                            componentDefinition);
                    componentDefinition.add(referenceTarget);
                } else {
                    throw new InvalidWireException("Source not found", sourceName);
                }
            }
        }
    }

    private ReferenceTarget createReferenceTarget(URI componentReferenceName,
                                                  URI target,
                                                  ComponentDefinition componentDefn) throws InvalidWireException {
        ComponentType componentType = componentDefn.getImplementation().getComponentType();
        if (componentReferenceName == null) {
            // if there is ambiguity in determining the source of the wire or there is no reference to be wired
            if (componentType.getReferences().size() > 1 || componentType.getReferences().isEmpty()) {
                throw new InvalidWireException("Unable to determine unique source reference");
            } else {
                Map references = componentType.getReferences();
                ReferenceDefinition definition = (ReferenceDefinition) references.values().iterator().next();
                componentReferenceName = definition.getUri();
            }
        }

        ReferenceTarget referenceTarget = new ReferenceTarget();
        referenceTarget.setReferenceName(componentReferenceName);
        referenceTarget.addTarget(target);
        return referenceTarget;
    }

    protected void verifyCompositeCompleteness(CompositeComponentType composite) throws InvalidServiceException {
        // check if all of the composite services have been wired
        for (ServiceDefinition svcDefn : composite.getDeclaredServices().values()) {
            if (svcDefn.getTarget() == null) {
                String identifier = svcDefn.getUri().toString();
                throw new InvalidServiceException("Composite service not wired to a target", identifier);
            }
        }
    }

    private void validateTarget(URI target, CompositeComponentType composite) throws InvalidWireException {
        // if target is not a reference of the composite
        String targetName = target.getPath();
        if (composite.getReferences().get(targetName) == null) {
            ComponentDefinition<?> targetDefinition = composite.getDeclaredComponents().get(targetName);
            // if a target component exists in this composite
            if (targetDefinition != null) {
                Implementation<?> implementation = targetDefinition.getImplementation();
                ComponentType<?, ?, ?> componentType = implementation.getComponentType();
                Map<String, ? extends ServiceDefinition> services = componentType.getServices();
                if (target.getFragment() == null) {
                    if (services.size() > 1 || services.isEmpty()) {
                        throw new InvalidWireException("Ambiguous target", target.toString());
                    }
                } else {
                    if (services.get(target.getFragment()) == null) {
                        throw new InvalidWireException("Invalid target service", target.toString());
                    }
                }
            } else {
                throw new InvalidWireException("Target not found", target.toString());
            }
        }
    }
}
