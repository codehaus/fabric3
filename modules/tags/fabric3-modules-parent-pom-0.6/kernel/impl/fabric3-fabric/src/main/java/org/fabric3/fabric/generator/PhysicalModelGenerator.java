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
package org.fabric3.fabric.generator;

import java.util.Collection;

import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.fabric.instantiator.LogicalChange;

/**
 * Interface that abstracts the concerns of a generating commands to provision a set of componets to runtimes in a domain.
 *
 * @version $Revision$ $Date$
 */
public interface PhysicalModelGenerator {

    /**
     * Generate commands to provision a set of components and their wires to runtimes in the domain based on the given set of logical components.
     *
     * @param components the logical component set.
     * @return the command map containing the generated commands
     * @throws GenerationException If unable to generate the command map.
     */
    CommandMap generate(Collection<LogicalComponent<?>> components) throws GenerationException;


    /**
     * Generate commands to provision a set of components and their wires to runtimes in the domain based on the given set of logical components.     *
     * @param change LogicalChange that encapsulates changes to components, properties & wires
     * @return the command map containing the generated commands
     * @throws GenerationException
     */
    CommandMap generate(LogicalChange change) throws GenerationException;

}
