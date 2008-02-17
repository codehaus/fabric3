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

import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Interface that abstracts the concerns of generating physical model 
 * from logical components. This is used from the assembly for 
 * creating physical model objects like component definitions, wire 
 * definitions etc from the logical model, before provisioning them to 
 * the participant nodes.
 * 
 * TODO Identify the contract required.
 * 
 * @version $Revision$ $Date$
 */
public interface PhysicalModelGenerator {

    /**
     * Generate the physical changeset for the set of logical components.
     * 
     * @param components Logical component set.
     * @return Physical changeset for each allocated runtime.
     * @throws ActivateException If unable to generate changeset.
     */
    Map<URI, GeneratorContext> generate(Collection<LogicalComponent<?>> components) throws ActivateException;

    /**
     * Provision the physical changeset to the participant nodes.
     * 
     * @param contexts Physical changesets to be provisioned.
     * @throws ActivateException If unable to provision changesets.
     */
    void provision(Map<URI, GeneratorContext> contexts) throws ActivateException;

    /**
     * Generate the change sets for the wires.
     * 
     * @param wires Wires for which change sets need to be created.
     * @return Physical changeset for the wires.
     */
    Map<URI, GeneratorContext> generate(Set<LogicalWire> logicalWires, LogicalComponent<?> logicalComponent) throws ActivateException;

}
