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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.fabric.assembly.allocator.AllocationException;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.assembly.store.AssemblyStore;
import org.fabric3.fabric.assembly.store.RecordException;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.CompositeComponentType;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ModelObject;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.Referenceable;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.util.UriHelper;

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
    protected final Allocator allocator;
    protected final RoutingService routingService;
    protected final MetaDataStore metadataStore;
    protected final PromotionNormalizer promotionNormalizer;
    protected LogicalComponent<CompositeImplementation> domain;
    protected Map<URI, LogicalComponent<?>> domainMap;
    protected AssemblyStore assemblyStore;

    public AbstractAssembly(URI domainUri,
                            GeneratorRegistry generatorRegistry,
                            WireResolver wireResolver,
                            PromotionNormalizer normalizer,
                            Allocator allocator,
                            RoutingService routingService,
                            AssemblyStore assemblyStore,
                            MetaDataStore metadataStore) {
        this.domainUri = domainUri;
        this.generatorRegistry = generatorRegistry;
        this.wireResolver = wireResolver;
        this.promotionNormalizer = normalizer;
        this.allocator = allocator;
        this.routingService = routingService;
        this.assemblyStore = assemblyStore;
        this.metadataStore = metadataStore;
        domainMap = new ConcurrentHashMap<URI, LogicalComponent<?>>();
    }

    public void initialize() throws AssemblyException {
        // read the logical model from the store
        domain = assemblyStore.read();
        // reindex the model
        for (LogicalComponent<?> child : domain.getComponents()) {
            addToDomainMap(child);
        }
        // regenerate the domain components
        if (!domain.getComponents().isEmpty()) {
            try {
                generateAndProvision(null, domain, true, true);
            } catch (GenerationException e) {
                throw new AssemblyException(e);
            } catch (RoutingException e) {
                throw new AssemblyException(e);
            }
        }
    }

    public LogicalComponent<CompositeImplementation> getDomain() {
        return domain;
    }

    public void activate(QName deployable, boolean include) throws ActivateException {
        Contribution contribution = metadataStore.resolve(deployable);
        if (contribution == null) {
            throw new ArtifactNotFoundException("Deployable composite not found for", deployable.toString());
        }
        ModelObject object = contribution.getType(deployable);
        if (!(object instanceof CompositeComponentType)) {
            throw new IllegalContributionTypeException("Deployable must be a composite", deployable.toString());
        }
        CompositeComponentType type = (CompositeComponentType) object;
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>("type", impl);
        activate(definition, include);
        try {
            // record the operation
            assemblyStore.store(domain);
        } catch (RecordException e) {
            throw new ActivateException("Error activating deployable", deployable.toString(), e);
        }
    }

    public void activate(ComponentDefinition<?> definition, boolean include) throws ActivateException {
        try {
            // instantiate a logical component from the definition
            LogicalComponent<?> component;
            if (include) {
                component = instantiate(domainUri, domain, definition);
            } else {
                URI baseUri = URI.create(domainUri + "/" + definition.getName());
                component = instantiate(baseUri, domain, definition);
            }
            // resolve wires in the logical component
            wireResolver.resolve(domain, component, include);
            normalize(component);
            // perform the inclusion, which will result in the generation of change sets provisioned to service nodes
            generateAndProvision(domain, component, include, false);
            // TODO only add when the service nodes have acked
            if (include) {
                for (LogicalComponent<?> child : component.getComponents()) {
                    domain.addComponent(child);
                    addToDomainMap(child);
                }
            } else {
                domain.addComponent(component);
                addToDomainMap(component);
            }
        } catch (ResolutionException e) {
            throw new ActivateException(e);
        } catch (RoutingException e) {
            throw new ActivateException(e);
        } catch (GenerationException e) {
            throw new ActivateException(e);
        } catch (AllocationException e) {
            throw new ActivateException(e);
        }
    }

    public void bindService(URI serviceUri, BindingDefinition bindingDefinition) throws BindException {
        URI defragmentedUri = UriHelper.getDefragmentedName(serviceUri);
        LogicalComponent<?> targetComponent = domainMap.get(defragmentedUri);
        if (targetComponent == null) {
            throw new BindException("Component not found", defragmentedUri.toString());
        }
        String fragment = serviceUri.getFragment();
        LogicalService service;
        if (fragment == null) {
            if (targetComponent.getServices().size() != 1) {
                String uri = serviceUri.toString();
                throw new BindException("Component must implement one service if no service name specified", uri);
            }
            Collection<LogicalService> services = targetComponent.getServices();
            service = services.iterator().next();
        } else {
            service = targetComponent.getService(serviceUri.getFragment());
            if (service == null) {
                throw new BindException("Service not found", defragmentedUri.toString());
            }
        }
        LogicalBinding<?> binding = new LogicalBinding<BindingDefinition>(bindingDefinition, service);
        PhysicalChangeSet changeSet = new PhysicalChangeSet();
        CommandSet commandSet = new CommandSet();
        GeneratorContext context = new DefaultGeneratorContext(changeSet, commandSet);
        try {
            generatorRegistry.generateBoundServiceWire(service, binding, targetComponent, context);
            routingService.route(targetComponent.getRuntimeId(), changeSet);
            service.addBinding(binding);
            // TODO record to recovery service
        } catch (GenerationException e) {
            throw new BindException("Error binding service", serviceUri.toString(), e);
        } catch (RoutingException e) {
            throw new BindException(e);
        }
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
        LogicalComponent<I> component = new LogicalComponent<I>(baseUri, runtimeId, definition, parent);
        if (CompositeComponentType.class.isInstance(type)) {
            CompositeComponentType compositeType = CompositeComponentType.class.cast(type);
            LogicalComponent<CompositeImplementation> composite =
                    (LogicalComponent<CompositeImplementation>) component;
            for (ComponentDefinition<? extends Implementation<?>> child : compositeType.getComponents().values()) {
                URI childUri = URI.create(baseUri.toString() + "/" + child.getName());
                LogicalComponent<? extends Implementation<?>> logicalChild = instantiate(childUri, composite, child);
                component.addComponent(logicalChild);
            }
        }
        for (ServiceDefinition service : type.getServices().values()) {
            URI serviceUri = baseUri.resolve('#' + service.getName());
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            if (service.getTarget() != null) {
                logicalService.setTargetUri(URI.create(baseUri.toString() + "/" + service.getTarget()));
            }
            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }
            component.addService(logicalService);
        }
        for (ReferenceDefinition reference : type.getReferences().values()) {
            URI referenceUri = baseUri.resolve('#' + reference.getName());
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);
            for (BindingDefinition binding : reference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            for (URI promotedUri : reference.getPromoted()) {
                URI resolvedUri = URI.create(baseUri.toString() + "/" + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }
            component.addReference(logicalReference);
        }
        return component;
    }

    /**
     * Normalizes the component and any children
     *
     * @param component the component to normalize
     */
    protected void normalize(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        ComponentType<?, ?, ?> type = implementation.getComponentType();
        if (CompositeComponentType.class.isInstance(type)) {
            for (LogicalComponent<?> child : component.getComponents()) {
                normalize(child);
            }
        } else {
            promotionNormalizer.normalize(component);
        }
    }

    /**
     * Generates and routes a physical change set to a set of service nodes based on the logical component.
     *
     * @param targetComposite     the target composite
     * @param component           the logical component generate physical artifacts frm
     * @param includeInDomain     true if SCA domain inclusion semantics should be in effect. That is, if true, children
     *                            will be included at the domain level.
     * @param synchronizeTopology true if the topological view of the service network should be synchronized during
     *                            allocation
     * @throws ResolutionException if an error ocurrs resolving a target
     * @throws GenerationException if an error is encountered generating a physical change set
     * @throws RoutingException    if an error is encountered routing the change set
     * @throws AllocationException if an error occurs during allocation
     */
    protected void generateAndProvision(LogicalComponent<CompositeImplementation> targetComposite,
                                        LogicalComponent<?> component,
                                        boolean includeInDomain,
                                        boolean synchronizeTopology)
            throws ResolutionException, GenerationException, RoutingException, AllocationException {
        Map<URI, GeneratorContext> contexts = new HashMap<URI, GeneratorContext>();
        // create physical component definitions for composite children
        if (includeInDomain) {
            ComponentDefinition<?> definition = component.getDefinition();
            Implementation<?> implementation = definition.getImplementation();
            assert CompositeImplementation.class.isInstance(implementation);
            //noinspection unchecked
            LogicalComponent<CompositeImplementation> composite =
                    (LogicalComponent<CompositeImplementation>) component;
            // allocate the components
            allocator.allocate(composite, synchronizeTopology);
            for (LogicalComponent<?> child : component.getComponents()) {
                List<LogicalComponent<CompositeImplementation>> composites =
                        new ArrayList<LogicalComponent<CompositeImplementation>>();
                composites.add(composite);
                composites.add(targetComposite);
                generateChangeSets(composites, child, contexts);
            }
            for (LogicalComponent<?> child : component.getComponents()) {
                generateCommandSets(child, contexts);
            }

        } else {
            allocator.allocate(component, synchronizeTopology);
            List<LogicalComponent<CompositeImplementation>> composites =
                    new ArrayList<LogicalComponent<CompositeImplementation>>();
            composites.add(targetComposite);
            generateChangeSets(composites, component, contexts);
            generateCommandSets(component, contexts);
        }
        // route the change sets to service nodes
        for (Map.Entry<URI, GeneratorContext> entry : contexts.entrySet()) {
            routingService.route(entry.getKey(), entry.getValue().getPhysicalChangeSet());
        }
        // route command sets
        for (Map.Entry<URI, GeneratorContext> entry : contexts.entrySet()) {
            routingService.route(entry.getKey(), entry.getValue().getCommandSet());
        }

    }

    protected void generateChangeSets(List<LogicalComponent<CompositeImplementation>> targetComposites,
                                      LogicalComponent<?> component,
                                      Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Implementation<?> implementation = definition.getImplementation();
        if (CompositeImplementation.class.isInstance(implementation)) {
            for (LogicalComponent<?> child : component.getComponents()) {
                // if the component is already running on a node (e.g. during recovery), skip provisioning
                if (child.isActive()) {
                    continue;
                }
                // generate changesets recursively for children
                //noinspection unchecked
                LogicalComponent<CompositeImplementation> composite =
                        (LogicalComponent<CompositeImplementation>) component;
                List<LogicalComponent<CompositeImplementation>> composites =
                        new ArrayList<LogicalComponent<CompositeImplementation>>();
                composites.add(composite);
                generateChangeSets(composites, child, contexts);
                generatePhysicalWires(composites, child, contexts);
            }
        } else {
            // leaf component, generate a physical component and update the change sets
            // if component is already running on a node (e.g. during recovery), skip provisioning
            if (component.isActive()) {
                return;
            }
            generatePhysicalComponent(component, contexts);
            generatePhysicalWires(targetComposites, component, contexts);
        }
    }

    /**
     * Generates physical wire definitions for a logical component, updating the GeneratorContext. Wire targets will be
     * resolved against the given parent.
     * <p/>
     *
     * @param targetComposites the composites to resolve against
     * @param component        the component to generate wires for
     * @param contexts         the GeneratorContexts to update with physical wire definitions
     * @throws GenerationException if an error occurs generating phyasical wire definitions
     * @throws ResolutionException if an error occurs resolving a wire target
     */
    protected void generatePhysicalWires(List<LogicalComponent<CompositeImplementation>> targetComposites,
                                         LogicalComponent<?> component,
                                         Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {
        URI runtimeId = component.getRuntimeId();
        GeneratorContext context = contexts.get(runtimeId);
        if (context == null) {
            PhysicalChangeSet changeSet = new PhysicalChangeSet();
            CommandSet commandSet = new CommandSet();
            context = new DefaultGeneratorContext(changeSet, commandSet);
            contexts.put(runtimeId, context);
        }
        for (LogicalReference entry : component.getReferences()) {
            if (entry.getBindings().isEmpty()) {
                for (URI uri : entry.getTargetUris()) {
                    Referenceable target = resolveTarget(uri, targetComposites);
                    if (target == null) {
                        String refUri = entry.getUri().toString();
                        throw new TargetNotFoundException("Target not found for reference " + refUri, uri.toString());
                    }
                    if (target instanceof LogicalComponent) {
                        LogicalComponent<?> targetComponent = (LogicalComponent<?>) target;
                        String serviceName = uri.getFragment();
                        LogicalReference reference = component.getReference(entry.getUri().getFragment());
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
                        String name = target.getClass().getName();
                        URI refUri = entry.getUri();
                        throw new InvalidTargetTypeException("Invalid reference target type", name, refUri, uri);
                    }

                }
            } else {
                // TODO this should be extensible and moved out
                LogicalBinding<?> logicalBinding = entry.getBindings().get(0);
                generatorRegistry.generateBoundReferenceWire(component, entry, logicalBinding, context);
            }

        }
        // generate changesets for bound service wires
        for (LogicalService service : component.getServices()) {
            List<LogicalBinding<?>> bindings = service.getBindings();
            if (bindings.isEmpty()) {
                // service is not bound, skip
                continue;
            }
            for (LogicalBinding<?> binding : service.getBindings()) {
                generatorRegistry.generateBoundServiceWire(service, binding, component, context);
            }
        }
    }

    protected void generateCommandSets(LogicalComponent<?> component,
                                       Map<URI, GeneratorContext> contexts) throws GenerationException {

        GeneratorContext context = contexts.get(component.getRuntimeId());
        if (context != null) {
            generatorRegistry.generateCommandSet(component, context);
            if (isEagerInit(component)) {
                // if the component is eager init, add it to the list of components to initialize on the node it
                // will be provisioned to
                CommandSet commandSet = context.getCommandSet();
                List<Command> set = commandSet.getCommands(CommandSet.Phase.LAST);
                boolean found = false;
                for (Command command : set) {
                    // check if the command exists, and if so update it
                    if (command instanceof InitializeComponentCommand) {
                        ((InitializeComponentCommand) command).addUri(component.getUri());
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // a previous command was not found so create one
                    // @FIXME a trailing slash is needed since group ids are set on ComponentDefinitions using URI#resolve(",")
                    URI groupId = URI.create(component.getParent().getUri().toString() + "/");
                    InitializeComponentCommand initCommand = new InitializeComponentCommand(groupId);
                    initCommand.addUri(component.getUri());
                    commandSet.add(CommandSet.Phase.LAST, initCommand);
                }
            }
        }
        for (LogicalComponent<?> child : component.getComponents()) {
            generateCommandSets(child, contexts);
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
            CommandSet commandSet = new CommandSet();
            context = new DefaultGeneratorContext(changeSet, commandSet);
            contexts.put(id, context);
        }
        generatorRegistry.generatePhysicalComponent(component, context);
    }

    /**
     * Recursively adds a logical component and its children to the component map
     *
     * @param component the component to add
     */
    protected void addToDomainMap(LogicalComponent<?> component) {
        domainMap.put(component.getUri(), component);
        for (LogicalComponent<?> child : component.getComponents()) {
            addToDomainMap(child);
        }
    }

    protected boolean isEagerInit(LogicalComponent<?> component) {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Integer level = definition.getInitLevel();
        if (level != null) {
            return level > 0;
        }
        return definition.getImplementation().getComponentType().getInitLevel() > 0;
    }

    /**
     * Subclasses are responsible for implementing an algorithm to resolve target URIs against a composite
     *
     * @param uri        the target uri to resolve
     * @param components the composites to resolve against, in order
     * @return the logical instance
     * @throws ResolutionException if an error occurs during resolution, such as the target not being found
     */
    protected abstract Referenceable resolveTarget(URI uri, List<LogicalComponent<CompositeImplementation>> components)
            throws ResolutionException;

}
