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
package org.fabric3.spi.generator;

import java.net.URI;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.BindingDefinition;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.IntentDefinition;
import org.fabric3.spi.model.type.ResourceDescription;

/**
 * A registry for {@link ComponentGenerator}s, {@link BindingGenerator}s, {@link InterceptorGenerator}s and {@link
 * ComponentResourceGenerator}s. Generators are responsible for producing physical model objects that are provisioned to
 * service nodes from their logical counterparts.
 *
 * @version $Rev$ $Date$
 */
public interface GeneratorRegistry {

    /**
     * Registers a component generator
     *
     * @param clazz     the implementation type the generator handles
     * @param generator the generator to register
     */
    <T extends Implementation<?>> void register(Class<T> clazz,
                                                ComponentGenerator<LogicalComponent<T>> generator);

    /**
     * Registers a component generator
     *
     * @param clazz     the binding type type the generator handles
     * @param generator the generator to register
     */
    <T extends BindingDefinition> void register(Class<T> clazz, BindingGenerator generator);

    /**
     * Registers an interceptor generator
     *
     * @param phase     the binding type type the generator handles
     * @param generator the generator to register
     */
    <T extends IntentDefinition> void register(Class<T> phase, InterceptorGenerator<T> generator);

    /**
     * Registers a resource generator
     *
     * @param clazz     the resource type the generator handles
     * @param generator the generator to register
     */
    void register(Class<?> clazz, ComponentResourceGenerator generator);

    /**
     * Registers a command generator
     *
     * @param generator the generator to register
     */
    void register(CommandGenerator generator);

    /**
     * Generates a PhysicalComponentDefinition from the logical component. A physical change set for the runtime the
     * component will be provisioned to is updated with the physical component definition.
     *
     * @param logical the
     * @param context the generator context containing the current physical changes sets
     * @throws GenerationException if an error ocurrs during generation
     */
    <C extends LogicalComponent<?>> void generatePhysicalComponent(C logical, GeneratorContext context)
            throws GenerationException;


    /**
     * Generates a PhysicalWireDefinition from a bound service to a component. A physical change set for the runtime the
     * wire will be provisioned to is updated with the physical wire definition
     *
     * @param service the logical service representing the wire source
     * @param binding the binding the wire will be attached to at its source
     * @param target  the target lgical component for the wire
     * @param context the generator context
     * @throws GenerationException if an error ocurrs during generation
     */
    <C extends LogicalComponent<?>> void generateBoundServiceWire(LogicalService service,
                                                                  LogicalBinding binding,
                                                                  C target,
                                                                  GeneratorContext context) throws GenerationException;


    /**
     * Generates a PhysicalWireDefinition from a bound service to a component. A physical change set for the runtime the
     * wire will be provisioned to is updated with the physical wire definition
     *
     * @param source    the source logical component for the wire
     * @param reference the component reference the wire is associated with to at its source
     * @param binding   the binding the wire will be attached to at its terminating end
     * @param context   the generator context
     * @throws GenerationException if an error ocurrs during generation
     */
    <C extends LogicalComponent<?>> void generateBoundReferenceWire(C source,
                                                                    LogicalReference reference,
                                                                    LogicalBinding binding,
                                                                    GeneratorContext context)
            throws GenerationException;

    /**
     * Generates a PhysicalWireDefinition for an unbound wire. Unbound wires are direct connections between two
     * components. A physical change set for the runtime the wire will be provisioned to is updated with the physical
     * wire definition
     *
     * @param source    the source component the wire will be attached to
     * @param reference the component reference the wire is associated with at its source
     * @param service   the component service the wire is associated with to at its terminating end
     * @param target    the target component the wire will be attached to
     * @param context   the generator context
     * @throws GenerationException if an error ocurrs during generation
     */
    <S extends LogicalComponent<?>, T extends LogicalComponent<?>> void generateUnboundWire(S source,
                                                                                            LogicalReference reference,
                                                                                            LogicalService service,
                                                                                            T target,
                                                                                            GeneratorContext context)
            throws GenerationException;

    /**
     * Generates a PhysicalResourceContainerDefinition from a ResourceDefinition. A physical change set for the runtime
     * the component will be provisioned to is updated with the physical resource definition
     *
     * @param context the generator context
     * @return a URI representing the unique identifier for the physical  resource
     * @throws GenerationException if an error ocurrs during generation
     */
    URI generateResource(ResourceDescription description, LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException;

    /**
     * Generates a CommandSet for provisioning the logical component
     *
     * @param component the logical component to generate the command set from
     * @param context   the generator context
     * @throws GenerationException if an error ocurrs during generation
     */
    void generateCommandSet(LogicalComponent<?> component, GeneratorContext context) throws GenerationException;

}
