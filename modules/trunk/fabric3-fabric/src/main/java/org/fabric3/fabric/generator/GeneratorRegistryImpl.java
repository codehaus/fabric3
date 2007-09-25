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
package org.fabric3.fabric.generator;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySetExtension;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.SCABindingDefinition;
import org.fabric3.spi.policy.registry.PolicyResolutionException;
import org.fabric3.spi.policy.registry.PolicyResolver;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

/**
 * @version $Rev$ $Date$
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {

    private Map<Class<?>,
            ComponentGenerator<? extends LogicalComponent<? extends Implementation<?>>>> componentGenerators;
    private Map<Class<? extends BindingDefinition>, BindingGenerator<?, ?, ? extends BindingDefinition>> bindingGenerators;
    private List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();
    private PolicyResolver policyResolver;
    private Map<Class<? extends PolicySetExtension>, InterceptorDefinitionGenerator<? extends PolicySetExtension, ?>> interceptorDefinitionGenerators;

    /**
     *
     */
    public GeneratorRegistryImpl() {

        componentGenerators =
                new ConcurrentHashMap<Class<?>,
                        ComponentGenerator<? extends LogicalComponent<? extends Implementation<?>>>>();

        bindingGenerators =
                new ConcurrentHashMap<Class<? extends BindingDefinition>,
                        BindingGenerator<?, ?, ? extends BindingDefinition>>();

        interceptorDefinitionGenerators =
                new ConcurrentHashMap<Class<? extends PolicySetExtension>,
                        InterceptorDefinitionGenerator<? extends PolicySetExtension, ?>>();

    }

    /**
     * @see org.fabric3.spi.generator.GeneratorRegistry#register(java.lang.Class,org.fabric3.spi.generator.BindingGenerator)
     */
    public <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator<?, ?, T> generator) {
        bindingGenerators.put(clazz, generator);
    }

    /**
     * @see org.fabric3.spi.generator.GeneratorRegistry#register(org.fabric3.spi.generator.CommandGenerator)
     */
    public void register(CommandGenerator generator) {
        commandGenerators.add(generator);
    }

    /**
     * @see org.fabric3.spi.generator.GeneratorRegistry#register(java.lang.Class,org.fabric3.spi.generator.ComponentGenerator)
     */
    public <T extends Implementation<?>> void register(Class<T> clazz, ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    /**
     * Injects the policy resolver.
     *
     * @param policyResolver Policy registry.
     */
    @Reference(required = true)
    public void setPolicyResolver(PolicyResolver policyResolver) {
        this.policyResolver = policyResolver;
    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> PhysicalComponentDefinition generatePhysicalComponent(C component, GeneratorContext context)
            throws GenerationException {
        ComponentDefinition definition = component.getDefinition();
        Class<?> type = definition.getImplementation().getClass();
        ComponentGenerator<C> generator = (ComponentGenerator<C>) componentGenerators.get(type);
        if (generator == null) {
            throw new GeneratorNotFoundException(type);
        }
        
        Set<Intent> intentsToBeProvided = null;
        try {
            intentsToBeProvided = policyResolver.getImplementationIntentsToBeProvided(component);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        return generator.generate(component, intentsToBeProvided, context);
        
    }

    /**
     * @see org.fabric3.spi.generator.GeneratorRegistry#generateResourceWires(
     *     org.fabric3.spi.model.instance.LogicalComponent, 
     *     org.fabric3.spi.generator.GeneratorContext)
     */
    public void generateResourceWires(LogicalComponent<?> logical, GeneratorContext context) {
    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                         LogicalBinding<?> binding,
                                                                         C component,
                                                                         GeneratorContext context)
            throws GenerationException {

        ServiceContract<?> contract = service.getDefinition().getServiceContract();

        Set<PolicySetExtension> policies = null;
        try {
            policies = policyResolver.resolveInteractionIntents(binding);
            policies.addAll(policyResolver.resolveImplementationIntents(component));
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);

        Class<?> type = component.getDefinition().getImplementation().getClass();
        ComponentGenerator<C> targetGenerator = (ComponentGenerator<C>) componentGenerators.get(type);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        URI targetUri = service.getPromote();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(targetService, component);
        wireDefinition.setTarget(targetDefinition);
        BindingGenerator sourceGenerator = bindingGenerators.get(binding.getBinding().getClass());
        if (sourceGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }

        Set<Intent> intentsToBeProvided = null;
        try {
            intentsToBeProvided = policyResolver.getInteractionIntentsToBeProvided(binding);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding, intentsToBeProvided, context, service.getDefinition());
        wireDefinition.setSource(sourceDefinition);
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundReferenceWire(C source,
                                                                           LogicalReference reference,
                                                                           LogicalBinding<?> binding,
                                                                           GeneratorContext context) throws GenerationException {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract();

        Set<PolicySetExtension> policies = null;
        try {
            policies = policyResolver.resolveInteractionIntents(binding);
        } catch(PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);

        Class<? extends BindingDefinition> bindingType = binding.getBinding().getClass();
        BindingGenerator targetGenerator = bindingGenerators.get(bindingType);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(bindingType);
        }


        Set<Intent> intentsToBeProvided = null;
        try {
            intentsToBeProvided = policyResolver.getInteractionIntentsToBeProvided(binding);
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(binding, intentsToBeProvided, context, reference.getDefinition());
        wireDefinition.setTarget(targetDefinition);

        Class<? extends Implementation> implType = source.getDefinition().getImplementation().getClass();
        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) componentGenerators.get(implType);
        if (sourceGenerator == null) {
            throw new GeneratorNotFoundException(implType);
        }
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, false);
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

        Set<PolicySetExtension> policies = new HashSet<PolicySetExtension>();
        
        try {
            policies.addAll(policyResolver.resolveInteractionIntents((new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, service))));
            policies.addAll(policyResolver.resolveInteractionIntents((new LogicalBinding<SCABindingDefinition>(SCABindingDefinition.INSTANCE, reference))));
            policies.addAll(policyResolver.resolveImplementationIntents((target)));
        } catch (PolicyResolutionException e) {
            throw new PolicyException(e);
        }
        
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, policies);

        Class<?> type = target.getDefinition().getImplementation().getClass();
        ComponentGenerator<T> targetGenerator = (ComponentGenerator<T>) componentGenerators.get(type);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        PhysicalWireTargetDefinition targetDefinition = targetGenerator.generateWireTarget(service, target);
        wireDefinition.setTarget(targetDefinition);

        type = source.getDefinition().getImplementation().getClass();
        ComponentGenerator<S> sourceGenerator = (ComponentGenerator<S>) componentGenerators.get(type);
        if (sourceGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
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
        
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(source, reference, optimizable);
        sourceDefinition.setKey(target.getDefinition().getKey());
        wireDefinition.setSource(sourceDefinition);
        
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    public void generateCommandSet(LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException {
        for (CommandGenerator generator : commandGenerators) {
            generator.generate(component, context);
        }

    }

    @SuppressWarnings("unchecked")
    public Set<PhysicalInterceptorDefinition> generateInterceptorDefinitions(Set<PolicySetExtension> policies) {

        if (policies == null) {
            return Collections.EMPTY_SET;
        }

        Set<PhysicalInterceptorDefinition> interceptors = new HashSet<PhysicalInterceptorDefinition>();
        for (PolicySetExtension policy : policies) {
            InterceptorDefinitionGenerator interceptorDefinitionGenerator =
                    interceptorDefinitionGenerators.get(policy.getClass());
            interceptors.add(interceptorDefinitionGenerator.generate(policy, null));
        }
        return interceptors;

    }

    public <T extends PolicySetExtension> void register(Class<T> clazz, InterceptorDefinitionGenerator<T, ?> interceptorDefinitionGenerator) {
        interceptorDefinitionGenerators.put(clazz, interceptorDefinitionGenerator);
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
    private PhysicalWireDefinition createWireDefinition(ServiceContract<?> contract, Set<PolicySetExtension> policies)
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

}
