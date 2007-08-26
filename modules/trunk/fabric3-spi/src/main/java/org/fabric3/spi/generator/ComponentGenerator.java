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

import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Implementations are responsible for generating {@link org.fabric3.spi.model.physical.PhysicalChangeSet} metadata used
 * to provision components to service nodes
 *
 * @version $Rev$ $Date$
 */
public interface ComponentGenerator<C extends LogicalComponent<? extends Implementation<?>>> {

    /**
     * Generates an {@link org.fabric3.spi.model.physical.PhysicalComponentDefinition} based on a {@link
     * org.fabric3.scdl.ComponentDefinition}. The resulting PhysicalComponentDefinition is added to the
     * PhysicalChangeSet associated with the current GeneratorContext.
     *
     * @param component the logical component to evaluate
     * @param intentsToBeProvided Intents that need to explicitly provided by the implementation.
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalComponentDefinition generate(C component, Set<QName> intentsToBeProvided, GeneratorContext context) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireSourceDefinition} used to attach a wire to a source component. Metadata contained
     * in the PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is
     * attached to its source on a service node.
     *
     * @param source      the logical component for the wire source
     * @param reference   the source logical reference
     * @param optimizable true is the wire may be optimized. ComponentGenerator implementations may decide to attach
     *                    optimizable wires is a specialized manner, such as by not generating proxies
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireSourceDefinition generateWireSource(C source, LogicalReference reference, boolean optimizable) throws GenerationException;

    /**
     * Generates a {@link PhysicalWireTargetDefinition} used to attach a wire to a target component. Metadata contained
     * in the PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is
     * attached to its target on a service node.
     *
     * @param service the target logical service
     * @param target  the logical component for the wire target
     * @return the metadata used to attach the wire to its target on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalWireTargetDefinition generateWireTarget(LogicalService service, C target) throws GenerationException;

}
