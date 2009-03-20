/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.policy.Policy;

/**
 * Generates {@link PhysicalWireSourceDefinition}s and {@link PhysicalWireTargetDefinition}s for a resolved binding.
 *
 * @version $Rev$ $Date$
 */
public interface BindingGenerator<BD extends BindingDefinition> {

    /**
     * Generates a physical wire source definition from a logical binding.
     *
     * @param binding           Logical binding.
     * @param policy            the policy metadata associated with the wire
     * @param serviceDefinition Service definition for the target.
     * @return Physical wire source definition.
     * @throws GenerationException if an error is raised during generation
     */
    PhysicalWireSourceDefinition generateWireSource(LogicalBinding<BD> binding, Policy policy, ServiceDefinition serviceDefinition)
            throws GenerationException;

    /**
     * Generates a physical wire target definition from a logical binding.
     *
     * @param binding  Logical binding.
     * @param policy   the policy metadata associated with the wire
     * @param contract the service contract of the wire
     * @return Physical wire target definition.
     * @throws GenerationException if an error is raised during generation
     */
    PhysicalWireTargetDefinition generateWireTarget(LogicalBinding<BD> binding, Policy policy, ServiceContract<?> contract)
            throws GenerationException;

}
