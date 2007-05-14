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
package org.fabric3.fabric.assembly;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.Referenceable;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ModelObject;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ResourceDescription;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Base class for abstract assemblies
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractAssembly implements Assembly {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");
    protected final URI domainUri;
    protected final GeneratorRegistry generatorRegistry;
    protected final WireResolver wireResolver;
    protected final RoutingService routingService;
    protected final MetaDataStore metadataStore;
    protected LogicalComponent<CompositeImplementation> domain;
    protected Map<String, RuntimeInfo> runtimes;

    public AbstractAssembly(URI domainUri,
                            GeneratorRegistry generatorRegistry,
                            WireResolver wireResolver,
                            RoutingService routingService,
                            MetaDataStore metadataStore) {
        this.domainUri = domainUri;
        this.generatorRegistry = generatorRegistry;
        this.wireResolver = wireResolver;
        this.routingService = routingService;
        this.metadataStore = metadataStore;
        domain = createDomain();
        runtimes = new ConcurrentHashMap<String, RuntimeInfo>();
    }

    public LogicalComponent<CompositeImplementation> getDomain() {
        return domain;
    }

    public void activate(QName deployable, boolean include) throws IncludeException {
        Contribution contribution = metadataStore.resolve(deployable);
        if (contribution == null) {
            throw new ArtifactNotFoundException("Deployable composite not found for", deployable.toString());
        }
        CompositeComponentType type = contribution.getComponentType(COMPOSITE, deployable);
        assert type != null;
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("type", impl);
        activate(definition, include);
    }

    public void activate(ComponentDefinition<?> definition, boolean include) throws IncludeException {
        try {
            // instantiate a logical component from the definition
            LogicalComponent<?> component;
            if (include) {
                component = instantiate(domainUri, domain, definition);
                // TODO only add when the service nodes have acked
                for (LogicalComponent<?> child : component.getComponents()) {
                    domain.addComponent(child);
                }
            } else {
                URI baseUri = URI.create(domainUri + "/" + definition.getName());
                component = instantiate(baseUri, domain, definition);
                // TODO only add when the service nodes have acked
                domain.addComponent(component);
            }
            // resolve wires in the logical component
            wireResolver.resolve(domain, component);
            // perform the inclusion, which will result in the generation of change sets provisioned to service nodes
            generateAndProvision(domain, component, true);
        } catch (ResolutionException e) {
            throw new IncludeException(e);
        } catch (RoutingException e) {
            throw new IncludeException(e);
        } catch (GenerationException e) {
            throw new IncludeException(e);
        }
    }

    public void registerRuntime(RuntimeInfo info) throws RuntimeRegistrationException {
        runtimes.put(info.getId(), info);
    }

    public Collection<RuntimeInfo> getRuntimes() {
        Set<String> runtimeNames = routingService.getRuntimeIds();
        Set<RuntimeInfo> runtimeIds = new HashSet<RuntimeInfo>(runtimeNames.size());
        for (String name : runtimeNames) {
            runtimeIds.add(new RuntimeInfo(name));
        }
        return runtimeIds;
    }

    public String resolveResourceContainer(ResourceDescription description, ModelObject type)
            throws ResourceResolutionException {
        throw new UnsupportedOperationException();
    }

    /**
     * Instantiates a logical component from a component definition
     *
     * @param parent     the parent logical component
     * @param definition the component definition to instantiate from
     * @param baseUri    the uri the  component  will be instantiated relative to
     * @return the instantiated logical component
     * @throws InstantiationException if an error occurs during instantiation
     */
    @SuppressWarnings({"unchecked"})
    protected <I extends Implementation<?>> LogicalComponent<I> instantiate(URI baseUri,
                                                                            LogicalComponent<CompositeImplementation> parent,
                                                                            ComponentDefinition<I> definition)
            throws InstantiationException {
        Implementation<?> impl = definition.getImplementation();
        ComponentType<?, ?, ?> type = impl.getComponentType();
        //URI uri = URI.create(baseUri.toString() + "/" + definition.getName());
        URI runtimeId = definition.getRuntimeId();
        LogicalComponent<I> component = new LogicalComponent<I>(baseUri, runtimeId, definition);
        component.setParent(parent);
        if (CompositeComponentType.class.isInstance(type)) {
            CompositeComponentType compositeType = CompositeComponentType.class.cast(type);
            LogicalComponent<CompositeImplementation> composite =
                    (LogicalComponent<CompositeImplementation>) component;
            for (ComponentDefinition<? extends Implementation<?>> child : compositeType.getComponents().values()) {
                URI childUri = URI.create(baseUri.toString() + "/" + child.getName());
                LogicalComponent<? extends Implementation<?>> logicalChild = instantiate(childUri, composite, child);
                logicalChild.setParent(composite);
                component.addComponent(logicalChild);
            }
        }
        for (ServiceDefinition service : type.getServices().values()) {
            URI serviceUri = baseUri.resolve(service.getUri());
            LogicalService logicalService = new LogicalService(serviceUri, service);
            if (service.getTarget() != null) {
                logicalService.setTargetUri(baseUri.resolve(service.getTarget()));
            }
            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding(binding));
            }
            component.addService(logicalService);
        }
        for (ReferenceDefinition reference : type.getReferences().values()) {
            URI referenceUri = baseUri.resolve(reference.getUri());
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference);
            for (BindingDefinition binding : reference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding(binding));
            }
            component.addReference(logicalReference);
        }
        return component;
    }

    /**
     * Generates and routes a physical change set to a set of service nodes based on the logical component.
     *
     * @param parent          the parent composite
     * @param component       the logical component generate physical artifacts frm
     * @param includeInDomain true if SCA domain inclusion semantics should be in effect. That is, if true, children
     *                        will be included at the domain level.
     * @throws ResolutionException if an error ocurrs resolving a target
     * @throws GenerationException if an error is encountered generating a physical change set
     * @throws RoutingException    if an error is encountered routing the change set
     */
    protected void generateAndProvision(LogicalComponent<CompositeImplementation> parent,
                                        LogicalComponent<?> component,
                                        boolean includeInDomain)
            throws ResolutionException, GenerationException, RoutingException {
        Map<URI, GeneratorContext> contexts = new HashMap<URI, GeneratorContext>();
        // create physical component definitions for composite children
        if (includeInDomain) {
            ComponentDefinition<?> definition = component.getDefinition();
            Implementation<?> implementation = definition.getImplementation();
            assert CompositeImplementation.class.isInstance(implementation);
            //noinspection unchecked
            LogicalComponent<CompositeImplementation> composite =
                    (LogicalComponent<CompositeImplementation>) component;
            for (LogicalComponent<?> child : component.getComponents()) {
                generateChangeSets(composite, child, contexts);
            }
        } else {
            generateChangeSets(parent, component, contexts);
        }
        // route the change sets to service nodes
        for (Map.Entry<URI, GeneratorContext> entry : contexts.entrySet()) {
            routingService.route(entry.getKey(), entry.getValue().getPhysicalChangeSet());
        }
    }

    protected void generateChangeSets(LogicalComponent<CompositeImplementation> parent,
                                      LogicalComponent<?> component,
                                      Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Implementation<?> implementation = definition.getImplementation();
        if (CompositeImplementation.class.isInstance(implementation)) {
            for (LogicalComponent<?> child : component.getComponents()) {
                // generate changesets recursively for children
                //noinspection unchecked
                LogicalComponent<CompositeImplementation> composite =
                        (LogicalComponent<CompositeImplementation>) component;
                generateChangeSets(composite, child, contexts);
                generatePhysicalWires(composite, child, contexts);
            }
        } else {
            // leaf component, generate a physical component and update the change sets
            generatePhysicalComponent(component, contexts);
            generatePhysicalWires(parent, component, contexts);
        }
    }

    /**
     * Generates physical wire definitions for a logical component, updating the GeneratorContext. Wire targets will be
     * resolved against the given parent.
     * <p/>
     *
     * @param parent    the composite to resolve against
     * @param component the component to generate wires for
     * @param contexts  the GeneratorContexts to update with physical wire definitions
     * @throws GenerationException if an error occurs generating phyasical wire definitions
     * @throws ResolutionException if an error occurs resolving a wire target
     */
    protected void generatePhysicalWires(LogicalComponent<CompositeImplementation> parent,
                                         LogicalComponent<?> component,
                                         Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {
        for (LogicalReference entry : component.getReferences()) {
            URI id = component.getRuntimeId();
            GeneratorContext context = contexts.get(id);
            if (context == null) {
                PhysicalChangeSet changeSet = new PhysicalChangeSet();
                context = new DefaultGeneratorContext(changeSet);
                contexts.put(id, context);
            }

            String referenceName = entry.getUri().getFragment();
            List<URI> targets = entry.getTargetUris();
            for (URI uri : targets) {
                Referenceable target = resolveTarget(uri, parent);
                if (target instanceof LogicalReference) {
                    // TODO this should be extensible and moved out
                    LogicalReference logicalReference = ((LogicalReference) target);
                    LogicalBinding logicalBinding = logicalReference.getBindings().get(0);
                    generatorRegistry.generateBoundReferenceWire(component, entry, logicalBinding, context);
                } else if (target instanceof LogicalComponent) {
                    LogicalComponent<?> targetComponent = (LogicalComponent) target;
                    String serviceName = uri.getFragment();
                    LogicalReference reference = component.getReference(referenceName);
                    LogicalService targetService = null;
                    if (serviceName != null) {
                        targetService = targetComponent.getService(serviceName);
                    } else if (targetComponent.getServices().size() == 1) {
                        // default service
                        targetService = targetComponent.getServices().iterator().next();
                    }
                    assert targetService != null;
                    generatorRegistry.generateUnboundWire(component,
                                                          reference,
                                                          targetService,
                                                          targetComponent,
                                                          context);
                } else {
                    throw new InvalidTargetTypeException("Invalid reference target type",
                                                         target.getClass().getName(),
                                                         entry.getUri(),
                                                         uri);
                }
            }
        }
        // generate changesets for bound service wires
        for (LogicalService service : component.getServices()) {
            if (service.getBindings().isEmpty()) {
                // service is not bound, skip
                continue;
            }
            URI uri = service.getTargetUri();
            Referenceable target = resolveTarget(uri, parent);
            assert target instanceof LogicalComponent;
            LogicalComponent<?> targetComponent = (LogicalComponent<?>) target;
            URI id = targetComponent.getRuntimeId();
            GeneratorContext context = contexts.get(id);
            if (context == null) {
                PhysicalChangeSet changeSet = new PhysicalChangeSet();
                context = new DefaultGeneratorContext(changeSet);
                contexts.put(id, context);
            }
            for (LogicalBinding binding : service.getBindings()) {
                generatorRegistry.generateBoundServiceWire(service, binding, targetComponent, context);
            }

        }
    }

    /**
     * Generates a physical component from the given logical component, updating the appropriate GeneratorContext or
     * creating a new one if necessary. A GeneratorContext is created for each service node a physical compnent is
     * provisioned to.
     * <p/>
     *
     * @param component the logical component to generate from
     * @param contexts  the collection of generator contexts
     * @throws GenerationException if an exception occurs during generation
     */
    protected void generatePhysicalComponent(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException {
        URI id = component.getRuntimeId();
        GeneratorContext context = contexts.get(id);
        if (context == null) {
            PhysicalChangeSet changeSet = new PhysicalChangeSet();
            context = new DefaultGeneratorContext(changeSet);
            contexts.put(id, context);
        }
        generatorRegistry.generatePhysicalComponent(component, context);
    }

    /**
     * Bootstraps the logical component representing the domain.
     *
     * @return the logical component representing the domain
     */
    protected LogicalComponent<CompositeImplementation> createDomain() {
        CompositeComponentType type = new CompositeComponentType();
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(domainUri.toString(), impl);
        return new LogicalComponent<CompositeImplementation>(domainUri, domainUri, definition);
    }

    /**
     * Subclasses are responsible for implementing an algorithm to resolve target URIs against a composite
     *
     * @param uri       the target uri to resolve
     * @param component the composite to resolve against
     * @return the logical instance
     * @throws ResolutionException if an error occurs during resolution, such as the target not being found
     */
    protected abstract Referenceable resolveTarget(URI uri, LogicalComponent<CompositeImplementation> component)
            throws ResolutionException;

}
