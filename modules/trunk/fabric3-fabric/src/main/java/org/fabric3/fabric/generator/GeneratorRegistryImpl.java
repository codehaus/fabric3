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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.fabric.model.NonBlockingIntentDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.ComponentGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorGenerator;
import org.fabric3.spi.generator.ResourceGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.IntentDefinition;
import org.fabric3.spi.model.type.Operation;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ResourceDefinition;
import org.fabric3.spi.model.type.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class GeneratorRegistryImpl implements GeneratorRegistry {

    private Map<Class<?>,
            ComponentGenerator<? extends LogicalComponent<? extends Implementation>>> componentGenerators;
    private Map<Class<?>, BindingGenerator> bindingGenerators;
    private Map<Class<?>, InterceptorGenerator<? extends IntentDefinition>> interceptorGenerators;
    private Map<Class<?>, ResourceGenerator> resourceGenerators;
    private List<CommandGenerator> commandGenerators = new ArrayList<CommandGenerator>();

    public GeneratorRegistryImpl() {
        componentGenerators =
                new ConcurrentHashMap<Class<?>,
                        ComponentGenerator<? extends LogicalComponent<? extends Implementation>>>();
        bindingGenerators = new ConcurrentHashMap<Class<?>, BindingGenerator>();
        resourceGenerators = new ConcurrentHashMap<Class<?>, ResourceGenerator>();
        interceptorGenerators = new ConcurrentHashMap<Class<?>, InterceptorGenerator<? extends IntentDefinition>>();
    }

    public <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator generator) {
        bindingGenerators.put(clazz, generator);
    }

    public <T extends IntentDefinition> void register(Class<T> clazz, InterceptorGenerator<T> generator) {
        interceptorGenerators.put(clazz, generator);
    }

    public void register(Class<?> clazz, ResourceGenerator generator) {
        resourceGenerators.put(clazz, generator);
    }

    public void register(CommandGenerator generator) {
        commandGenerators.add(generator);
    }

    public <T extends Implementation<?>> void register(Class<T> clazz,
                                                       ComponentGenerator<LogicalComponent<T>> generator) {
        componentGenerators.put(clazz, generator);
    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generatePhysicalComponent(C component, GeneratorContext context)
            throws GenerationException {
        ComponentDefinition definition = component.getDefinition();
        Class<?> type = definition.getImplementation().getClass();
        ComponentGenerator<C> generator = (ComponentGenerator<C>) componentGenerators.get(type);
        if (generator == null) {
            throw new GeneratorNotFoundException(type);
        }
        generator.generate(component, context);
    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                         LogicalBinding binding,
                                                                         C component,
                                                                         GeneratorContext context)
            throws GenerationException {

        ServiceContract<?> contract = service.getDefinition().getServiceContract();
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, context);
        Class<?> type = component.getDefinition().getImplementation().getClass();
        ComponentGenerator<C> targetGenerator = (ComponentGenerator<C>) componentGenerators.get(type);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        URI targetUri = service.getTargetUri();
        LogicalService targetService;
        if (targetUri == null) {
            // the service is on the component
            targetService = service;
        } else {
            // service is defined on a composite and wired to a component service
            targetService = component.getService(targetUri.getFragment());
        }
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(targetService, component, context);
        wireDefinition.setTarget(targetDefinition);
        BindingGenerator sourceGenerator = bindingGenerators.get(binding.getBinding().getClass());
        if (sourceGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        PhysicalWireSourceDefinition sourceDefinition = sourceGenerator.generateWireSource(binding,
                                                                                           context,
                                                                                           service.getDefinition());
        wireDefinition.setSource(sourceDefinition);
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);

    }

    @SuppressWarnings({"unchecked"})
    public <C extends LogicalComponent<?>>
    void generateBoundReferenceWire(C source,
                                    LogicalReference reference,
                                    LogicalBinding bindingDefinition,
                                    GeneratorContext context) throws GenerationException {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, context);
        Class<?> type = bindingDefinition.getBinding().getClass();
        BindingGenerator targetGenerator = bindingGenerators.get(type);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(bindingDefinition, context, reference.getDefinition());
        wireDefinition.setTarget(targetDefinition);

        type = source.getDefinition().getImplementation().getClass();
        ComponentGenerator<C> sourceGenerator = (ComponentGenerator<C>) componentGenerators.get(type);
        if (sourceGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateWireSource(source, reference, false, context);
        wireDefinition.setSource(sourceDefinition);

        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);
    }

    @SuppressWarnings({"unchecked"})
    public <S extends LogicalComponent<?>, T extends LogicalComponent<?>>
    void generateUnboundWire(S source,
                             LogicalReference reference,
                             LogicalService service,
                             T target,
                             GeneratorContext context) throws GenerationException {
        ReferenceDefinition referenceDefinition = reference.getDefinition();
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        PhysicalWireDefinition wireDefinition = createWireDefinition(contract, context);
        Class<?> type = target.getDefinition().getImplementation().getClass();
        ComponentGenerator<T> targetGenerator = (ComponentGenerator<T>) componentGenerators.get(type);
        if (targetGenerator == null) {
            throw new GeneratorNotFoundException(type);
        }
        PhysicalWireTargetDefinition targetDefinition =
                targetGenerator.generateWireTarget(service, target, context);
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
        PhysicalWireSourceDefinition sourceDefinition =
                sourceGenerator.generateWireSource(source, reference, optimizable, context);
        wireDefinition.setSource(sourceDefinition);
        context.getPhysicalChangeSet().addWireDefinition(wireDefinition);
    }

    public URI generateResource(ResourceDefinition definition, GeneratorContext context) throws GenerationException {
        Class<?> type = definition.getClass();
        ResourceGenerator generator = resourceGenerators.get(type);
        if (generator == null) {
            throw new GeneratorNotFoundException(type);
        }
        return generator.generate(definition, context);
    }

    public void generatorCommandSet(LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException {
        for (CommandGenerator generator : commandGenerators) {
            generator.generate(component, context);
        }

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
    private PhysicalWireDefinition createWireDefinition(ServiceContract<?> contract, GeneratorContext context)
            throws GenerationException {
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition();
        for (Operation o : contract.getOperations()) {
            PhysicalOperationDefinition physicalOperation = mapOperation(o);
            wireDefinition.addOperation(physicalOperation);
            if (o.isNonBlocking()) {
                // this is egregious
                // hardcode intent until we get the intent infrastructure in place
                IntentDefinition intent = new NonBlockingIntentDefinition();
                Class<? extends IntentDefinition> type = NonBlockingIntentDefinition.class;
                InterceptorGenerator generator = interceptorGenerators.get(type);
                if (generator == null) {
                    throw new GeneratorNotFoundException(type);
                }
                PhysicalInterceptorDefinition interceptorDefinition = generator.generate(intent, context);
                physicalOperation.addInterceptor(interceptorDefinition);
            }
        }
        for (Operation o : contract.getCallbackOperations()) {
            PhysicalOperationDefinition physicalOperation = mapOperation(o);
            physicalOperation.setCallback(true);
            wireDefinition.addOperation(physicalOperation);
            if (o.isNonBlocking()) {
                // this is egregious
                // hardcode intent until we get the intent infrastructure in place
                IntentDefinition intent = new NonBlockingIntentDefinition();
                Class<? extends IntentDefinition> type = NonBlockingIntentDefinition.class;
                InterceptorGenerator generator = interceptorGenerators.get(type);
                if (generator == null) {
                    throw new GeneratorNotFoundException(type);
                }
                PhysicalInterceptorDefinition interceptorDefinition = generator.generate(intent, context);
                physicalOperation.addInterceptor(interceptorDefinition);
            }
        }
        return wireDefinition;
    }

}
