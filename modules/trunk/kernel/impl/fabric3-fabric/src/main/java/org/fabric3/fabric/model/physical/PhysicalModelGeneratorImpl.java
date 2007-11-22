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
package org.fabric3.fabric.model.physical;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fabric3.fabric.assembly.resolver.ResolutionException;
import org.fabric3.fabric.command.InitializeComponentCommand;
import org.fabric3.fabric.domain.DomainService;
import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.fabric.generator.PolicyException;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.Bindable;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.SCABindingDefinition;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
import org.fabric3.spi.policy.registry.PolicyResolver;
import org.fabric3.spi.util.UriHelper;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Default implementation of the physical model generator.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class PhysicalModelGeneratorImpl implements PhysicalModelGenerator {

    private final GeneratorRegistry generatorRegistry;
    private final PolicyResolver policyResolver;
    private final RoutingService routingService;
    private final DomainService domainService;
    
    /**
     * Injects generator registry and assembly store.
     * 
     * @param generatorRegistry Generator registry.
     * @param assemblyStore Assembly store.
     */
    public PhysicalModelGeneratorImpl(@Reference GeneratorRegistry generatorRegistry,
                                      @Reference PolicyResolver policyResolver,
                                      @Reference RoutingService routingService,
                                      @Reference DomainService domainService) {
        this.generatorRegistry = generatorRegistry;
        this.policyResolver = policyResolver;
        this.routingService = routingService;
        this.domainService = domainService;
    }

    /**
     * Generate and provision physical change sets for a set of new components.
     *
     * @param components the components to generate
     * @return a Map of Generation contexts keyed by runtimeId
     * @throws ActivateException if there was a problem
     */
    public Map<URI, GeneratorContext> generate(Collection<LogicalComponent<?>> components) throws ActivateException {
        Map<URI, GeneratorContext> contexts = new HashMap<URI, GeneratorContext>();
        try {
            for (LogicalComponent<?> component : components) {
                generateChangeSets(component, contexts);
            }
            for (LogicalComponent<?> component : components) {
                generateCommandSets(component, contexts);
            }
        } catch (GenerationException e) {
            throw new ActivateException(e);
        } catch (ResolutionException e) {
            throw new ActivateException(e);
        }
        return contexts;
    }

    public void provision(Map<URI, GeneratorContext> contexts) throws ActivateException {
        // provision the generated change sets
        try {
            // route the change sets to service nodes
            for (Map.Entry<URI, GeneratorContext> entry : contexts.entrySet()) {
                routingService.route(entry.getKey(), entry.getValue().getPhysicalChangeSet());
            }
            // route command sets
            for (Map.Entry<URI, GeneratorContext> entry : contexts.entrySet()) {
                routingService.route(entry.getKey(), entry.getValue().getCommandSet());
            }
        } catch (RoutingException e) {
            throw new ActivateException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <C extends LogicalComponent<?>> void generateResourceWire(C source,
                                                                     LogicalResource<?> resource,
                                                                     GeneratorContext context)
            throws GenerationException {

        // Generates the source side of the wire
        
        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(source.getDefinition().getImplementation().getClass());
        PhysicalWireSourceDefinition pwsd = sourceGenerator.generateResourceWireSource(source, resource, context);

        // Generates the target side of the wire
        ResourceWireGenerator targetGenerator = 
            generatorRegistry.getResourceWireGenerator(resource.getResourceDefinition().getClass());
        PhysicalWireTargetDefinition pwtd = targetGenerator.genearteWireTargetDefinition(resource, context);
        pwsd.setOptimizable(pwtd.getUri() != null);

        // Create the wire from the component to the resource
        ServiceContract<?> serviceContract = resource.getResourceDefinition().getServiceContract();
        PhysicalWireDefinition pwd = createWireDefinition(serviceContract, Collections.EMPTY_SET);
        pwd.setSource(pwsd);
        pwd.setTarget(pwtd);

        context.getPhysicalChangeSet().addWireDefinition(pwd);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> PhysicalComponentDefinition generatePhysicalComponent(C component,
                                                                                                 GeneratorContext context)
            throws GenerationException {
        
        ComponentGenerator<C> generator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(component.getDefinition().getImplementation().getClass());

        // Gather the implementation intents to be natively provided by the component implementation
        Set<Intent> intentsToBeProvided;
        try {
            intentsToBeProvided = policyResolver.getImplementationIntentsToBeProvided(component);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        return generator.generate(component, intentsToBeProvided, context);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                         LogicalBinding<?> binding,
                                                                         C component,
                                                                         GeneratorContext context)
            throws GenerationException {

        // use the service contract from the binding's parent service if it is defined, otherwise default to the one
        // defined on the original component
        ServiceContract contract;
        Bindable bindable = binding.getParent();
        assert bindable instanceof LogicalService;
        LogicalService logicalService = (LogicalService) bindable;
        
        ServiceContract<?> promotedContract = logicalService.getDefinition().getServiceContract();
        if (promotedContract == null) {
            contract = service.getDefinition().getServiceContract();
        } else {
            contract = promotedContract;
        }
        
        // Resolve the policies that map to interaction and implementation intents
        Set<PolicySet> policies;
        try {
            policies = policyResolver.resolveInteractionIntents(binding);
            policies.addAll(policyResolver.resolveImplementationIntents(component));
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);
        
        ComponentGenerator<C> targetGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(component.getDefinition().getImplementation().getClass());
        
        URI targetUri = service.getPromote();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, component, context);
        wireDefinition.setTarget(targetDefinition);
        
        BindingGenerator sourceGenerator = generatorRegistry.getBindingGenerator(binding.getBinding().getClass());

        // Resolve interaction intents to be provided by the binding
        Set<Intent> intentsToBeProvided;
        try {
            intentsToBeProvided = policyResolver.getInteractionIntentsToBeProvided(binding);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding,
                                                                                           intentsToBeProvided,
                                                                                           context,
                                                                                           service.getDefinition());
        wireDefinition.setSource(sourceDefinition);
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundReferenceWire(C source,
                                                                           LogicalReference reference,
                                                                           LogicalBinding<?> binding,
                                                                           GeneratorContext context)
            throws GenerationException {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract();

        // Resolve policies that map to interaction intents
        Set<PolicySet> policies;
        try {
            policies = policyResolver.resolveInteractionIntents(binding);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);

        BindingGenerator targetGenerator = generatorRegistry.getBindingGenerator(binding.getBinding().getClass());

        // Resolve interaction intents to be provided by the binding
        Set<Intent> intentsToBeProvided;
        try {
            intentsToBeProvided = policyResolver.getInteractionIntentsToBeProvided(binding);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(binding, intentsToBeProvided, context, reference.getDefinition());
        wireDefinition.setTarget(targetDefinition);

        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) 
            generatorRegistry.getComponentGenerator(source.getDefinition().getImplementation().getClass());
        
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, false, context);
        wireDefinition.setSource(sourceDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>> void generateUnboundWire(S source,
                                                                                                   LogicalReference reference,
                                                                                                   LogicalService service,
                                                                                                   T target,
                                                                                                   GeneratorContext context)
            throws GenerationException {

        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> contract = referenceDefinition.getServiceContract();

        Set<PolicySet> policies = new HashSet<PolicySet>();

        // Resolve policies on both ends of the wire that map to interaction and implementation intents
        try {
            policies.addAll(policyResolver.resolveInteractionIntents((new LogicalBinding<SCABindingDefinition>(
                    SCABindingDefinition.INSTANCE,
                    service))));
            policies.addAll(policyResolver.resolveInteractionIntents((new LogicalBinding<SCABindingDefinition>(
                    SCABindingDefinition.INSTANCE,
                    reference))));
            policies.addAll(policyResolver.resolveImplementationIntents((target)));
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }

        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);

        ComponentGenerator<T> targetGenerator = (ComponentGenerator<T>) 
            generatorRegistry.getComponentGenerator(target.getDefinition().getImplementation().getClass());

        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, target, context);
        wireDefinition.setTarget(targetDefinition);

        ComponentGenerator<S> sourceGenerator = (ComponentGenerator<S>) 
            generatorRegistry.getComponentGenerator(source.getDefinition().getImplementation().getClass());

        // determine if it is optimizable
        boolean optimizable = !contract.isConversational() && !contract.isRemotable();
        if (optimizable) {
            for (PhysicalOperationDefinition operation : wireDefinition.getOperations()) {
                if (!operation.getInterceptors().isEmpty()) {
                    optimizable = false;
                    break;
                }
            }
        }

        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, optimizable, context);
        sourceDefinition.setKey(target.getDefinition().getKey());
        wireDefinition.setSource(sourceDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    public void generateCommandSet(LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException {
        for (CommandGenerator generator : generatorRegistry.getCommandGenerators()) {
            generator.generate(component, context);
        }

    }

    @SuppressWarnings("unchecked")
    public Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(Set<PolicySet> policies) throws GenerationException {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();
        for (PolicySet policy : policies) {
            InterceptorDefinitionGenerator interceptorDefinitionGenerator = 
                generatorRegistry.getInterceptorDefinitionGenerator(policy.getExtensionName());
            interceptors.add(interceptorDefinitionGenerator.generate(policy, null));
        }
        return interceptors;

    }

    @SuppressWarnings({"unchecked"})
    private PhysicalOperationDefinition mapOperation(Operation o) {

        PhysicalOperationDefinition operation = new PhysicalOperationDefinition();
        operation.setName(o.getName());
        operation.setConversationSequence(o.getConversationSequence());
        Type returnType = o.getOutputType().getPhysical();
        operation.setReturnType(getClassName(returnType));

        DataType<List<? extends DataType<?>>> params = o.getInputType();
        for (DataType<?> param : params.getLogical()) {
            Type paramType = param.getPhysical();
            operation.addParameter(getClassName(paramType));
        }
        return operation;

    }

    @SuppressWarnings("unchecked")
    private String getClassName(Type paramType) {

        // TODO this needs to be fixed
        if (paramType instanceof Class) {
            return ((Class) paramType).getName();
        } else if (paramType instanceof ParameterizedType) {
            Type type = ((ParameterizedType) paramType).getRawType();
            if (type instanceof Class) {
                return ((Class) type).getName();
            }
        } else if (paramType instanceof TypeVariable) {
            TypeVariable var = (TypeVariable) paramType;
            if (var.getBounds().length > 0 && var.getBounds()[0] instanceof Class) {
                return ((Class) var.getBounds()[0]).getName();
            } else if (var.getBounds().length > 0 && var.getBounds()[0] instanceof ParameterizedType) {
                Type actualType = ((ParameterizedType) var.getBounds()[0]).getRawType();
                if (!(actualType instanceof Class)) {
                    throw new AssertionError();
                }
                return ((Class) actualType).getName();
            }
        } else if (paramType instanceof GenericArrayType) {
            GenericArrayType var = (GenericArrayType) paramType;
            return "[L" + var.getGenericComponentType();
        }
        throw new AssertionError();

    }

    @SuppressWarnings({"unchecked"})
    private PhysicalWireDefinition createWireDefinition(ServiceContract<?> contract, Set<PolicySet> policies)
            throws GenerationException {

        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();
        for (Operation o : contract.getOperations()) {
            PhysicalOperationDefinition physicalOperation = mapOperation(o);
            wireDefinition.addOperation(physicalOperation);
            for (PhysicalInterceptorDefinition interceptorDefinition : generateInterceptorDefinitions(policies)) {
                physicalOperation.addInterceptor(interceptorDefinition);
            }
        }

        for (Operation o : contract.getCallbackOperations()) {
            PhysicalOperationDefinition physicalOperation = mapOperation(o);
            physicalOperation.setCallback(true);
            wireDefinition.addOperation(physicalOperation);
        }

        return wireDefinition;

    }

    private void generateChangeSets(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException, ResolutionException {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Implementation<?> implementation = definition.getImplementation();
        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(implementation.getType())) {
            for (LogicalComponent<?> child : component.getComponents()) {
                // if the component is already running on a node (e.g. during recovery), skip provisioning
                if (child.isActive()) {
                    continue;
                }
                // generate changesets recursively for children
                generateChangeSets(child, contexts);
            }
        } else {
            // leaf component, generate a physical component and update the change sets
            // if component is already running on a node (e.g. during recovery), skip provisioning
            if (component.isActive()) {
                return;
            }
            generatePhysicalComponent(component, contexts);
            generatePhysicalWires(component, contexts);
        }
    }

    /**
     * Generates physical wire definitions for a logical component, updating the GeneratorContext. Wire targets will be
     * resolved against the given parent.
     * <p/>
     *
     * @param component the component to generate wires for
     * @param contexts  the GeneratorContexts to update with physical wire definitions
     * @throws GenerationException if an error occurs generating phyasical wire definitions
     * @throws org.fabric3.fabric.assembly.resolver.ResolutionException
     *                             if an error occurs resolving a wire target
     */
    private void generatePhysicalWires(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
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
                    LogicalComponent<?> target = domainService.findComponent(uri);
                    String serviceName = uri.getFragment();
                    if(target == null) {
                        System.err.println(uri);
                    }
                    LogicalService targetService = target.getService(serviceName);
                    assert targetService != null;
                    while (CompositeImplementation.class.isInstance(target.getDefinition().getImplementation())) {
                        URI promoteUri = targetService.getPromote();
                        URI promotedComponent = UriHelper.getDefragmentedName(promoteUri);
                        target = target.getComponent(promotedComponent);
                        targetService = target.getService(promoteUri.getFragment());
                    }
                    LogicalReference reference = component.getReference(entry.getUri().getFragment());

                    generateUnboundWire(component, reference, targetService, target, context);

                }
            } else {
                // TODO this should be extensible and moved out
                LogicalBinding<?> logicalBinding = entry.getBindings().get(0);
                generateBoundReferenceWire(component, entry, logicalBinding, context);
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
                generateBoundServiceWire(service, binding, component, context);
            }
        }

        // generate wire definitions for resources
        for (LogicalResource<?> resource : component.getResources()) {
            generateResourceWire(component, resource, context);
        }

    }

    private void generateCommandSets(LogicalComponent<?> component,
                                       Map<URI, GeneratorContext> contexts) throws GenerationException {

        GeneratorContext context = contexts.get(component.getRuntimeId());
        if (context != null) {
            generateCommandSet(component, context);
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
    private void generatePhysicalComponent(LogicalComponent<?> component, Map<URI, GeneratorContext> contexts)
            throws GenerationException {
        URI id = component.getRuntimeId();
        GeneratorContext context = contexts.get(id);
        if (context == null) {
            PhysicalChangeSet changeSet = new PhysicalChangeSet();
            CommandSet commandSet = new CommandSet();
            context = new DefaultGeneratorContext(changeSet, commandSet);
            contexts.put(id, context);
        }
        context.getPhysicalChangeSet().addComponentDefinition(generatePhysicalComponent(component, context));
    }

    private boolean isEagerInit(LogicalComponent<?> component) {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getImplementation().getComponentType();
        if (!componentType.getImplementationScope().equals(Scope.COMPOSITE)) {
            return false;
        }

        Integer level = definition.getInitLevel();
        if (level == null) {
            level = componentType.getInitLevel();
        }
        return level > 0;
    }

}
