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

import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Generates {@link PhysicalWireSourceDefinition}s and {@link PhysicalWireTargetDefinition}s for a resolved binding
 *
 * @version $Rev$ $Date$
 */
public interface BindingGenerator<PWSD extends PhysicalWireSourceDefinition, PWTD extends PhysicalWireTargetDefinition, BD extends BindingDefinition> {

    /**
     * Generates a physical wire source definition from a logical binding.
     *
     * @param binding Logical binding.
     * @param intentsToBeProvided Intents to be provided explictly by the binding.
     * @param serviceDefinition Service definition for the target.
     * @return Physical wire source definition.
     * @throws GenerationException
     */
    PWSD generateWireSource(LogicalBinding<BD> binding, 
                            Set<Intent> intentsToBeProvided,
                            Set<PolicySet> policySetsToBeProvided,
                            GeneratorContext context,
                            ServiceDefinition serviceDefinition) throws GenerationException;

    /**
     * Generates a physical wire target definition from a logical binding.
     *
     * @param binding Logical binding.
     * @param intentsToBeProvided Intents to be provided explictly by the binding.
     * @param referenceDefinition Reference definition for the target.
     * @return Physical wire target definition.
     * @throws GenerationException
     */
    PWTD generateWireTarget(LogicalBinding<BD> binding, 
                            Set<Intent> intentsToBeProvided,  
                            Set<PolicySet> policySetsToBeProvided,
                            GeneratorContext context,
                            ReferenceDefinition referenceDefinition) throws GenerationException;

}
