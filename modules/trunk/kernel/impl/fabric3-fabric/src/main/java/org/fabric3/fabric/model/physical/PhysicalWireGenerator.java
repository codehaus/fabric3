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

import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Revision$ $Date$
 */
public interface PhysicalWireGenerator {
    
    /**
     * Generates the physical wires for the resources in this component.
     * 
     * @param source Source component.
     * @param resourceDefinition Resource definition.
     * @param context Generator context.
     */
    <C extends LogicalComponent<?>> void generateResourceWire(C source, 
                                                              LogicalResource<?> resource, 
                                                              GeneratorContext context) throws GenerationException;


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
                                                                  LogicalBinding<?> binding,
                                                                  C target,
                                                                  GeneratorContext context) 
            throws GenerationException;


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
                                                                    LogicalBinding<?> binding,
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

}
