/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.spi.generator;

import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Implementations are responsible for generating command metadata used to provision components to runtimes.
 *
 * @version $Rev$ $Date$
 */
public interface ComponentGenerator<C extends LogicalComponent<? extends Implementation<?>>> {

    /**
     * Generates an {@link org.fabric3.spi.model.physical.PhysicalComponentDefinition} based on a {@link ComponentDefinition}. The resulting
     * PhysicalComponentDefinition is added to the PhysicalChangeSet associated with the current GeneratorContext.
     *
     * @param component the logical component to evaluate
     * @return the physical component definition
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalComponentDefinition generate(C component) throws GenerationException;

    /**
     * Generates a {@link PhysicalSourceDefinition} used to attach a wire to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a service
     * node.
     *
     * @param reference the source logical reference
     * @param policy    the provided intents and policy sets
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateWireSource(LogicalReference reference, EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a {@link PhysicalTargetDefinition} used to attach a wire to a target component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its target on a service
     * node.
     *
     * @param service the target logical service
     * @param policy  the provided intents and policy sets
     * @return the metadata used to attach the wire to its target on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalTargetDefinition generateWireTarget(LogicalService service, EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a {@link PhysicalSourceDefinition} used to attach a wire for a callback service to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a service
     * node.
     *
     * @param source          the logical component for the wire source
     * @param serviceContract callback service contract
     * @param policy          the provided intents and policy sets
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateCallbackWireSource(C source, ServiceContract<?> serviceContract, EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a {@link PhysicalSourceDefinition} used to attach a resource to a source component. Metadata contained in the
     * PhysicalWireSourceDefinition is specific to the component implementation type and used when the wire is attached to its source on a service
     * node.
     *
     * @param resource the source logical resource
     * @return the metadata used to attach the wire to its source on the service node
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalSourceDefinition generateResourceWireSource(LogicalResource<?> resource) throws GenerationException;

}
