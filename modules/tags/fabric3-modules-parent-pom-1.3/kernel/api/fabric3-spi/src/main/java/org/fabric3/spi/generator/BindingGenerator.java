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

import java.util.List;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.policy.EffectivePolicy;

/**
 * Generates {@link PhysicalSourceDefinition}s and {@link PhysicalTargetDefinition}s for a resolved binding.
 *
 * @version $Rev$ $Date$
 */
public interface BindingGenerator<BD extends BindingDefinition> {

    /**
     * Generates a physical wire source definition from a logical binding.
     *
     * @param binding    Logical binding.
     * @param contract   the service contract
     * @param operations the operations to generate the wire for
     * @param policy     the effective policy associated with the wire
     * @return Physical wire source definition.
     * @throws GenerationException if an error is raised during generation
     */
    PhysicalSourceDefinition generateWireSource(LogicalBinding<BD> binding,
                                                ServiceContract contract,
                                                List<LogicalOperation> operations,
                                                EffectivePolicy policy) throws GenerationException;

    /**
     * Generates a physical wire target definition from a logical binding.
     *
     * @param binding    Logical binding.
     * @param contract   the service contract
     * @param operations the operations to generate the wire for
     * @param policy     the effective policy associated with the wire
     * @return Physical wire target definition.
     * @throws GenerationException if an error is raised during generation
     */
    PhysicalTargetDefinition generateWireTarget(LogicalBinding<BD> binding,
                                                ServiceContract contract,
                                                List<LogicalOperation> operations,
                                                EffectivePolicy policy) throws GenerationException;

}
