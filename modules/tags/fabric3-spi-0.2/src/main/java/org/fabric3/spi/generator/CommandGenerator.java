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
package org.fabric3.spi.generator;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Generates CommandSets that must be applied on a service node after a PhysicalChangeSet has been provisioned.
 *
 * @version $Rev$ $Date$
 */
public interface CommandGenerator {

    /**
     * Generates an {@link org.fabric3.spi.command.CommandSet} based on a {@link
     * org.fabric3.scdl.ComponentDefinition}. The resulting CommandSet is added to the PhysicalChangeSet
     * associated with the current GeneratorContext.
     *
     * @param component the logical component to evaluate
     * @param context   the current generator context, which contains the CommandSet to be marshalled
     * @throws GenerationException if an error occurs during the generation process
     */
    void generate(LogicalComponent<?> component, GeneratorContext context)
            throws GenerationException;

}
