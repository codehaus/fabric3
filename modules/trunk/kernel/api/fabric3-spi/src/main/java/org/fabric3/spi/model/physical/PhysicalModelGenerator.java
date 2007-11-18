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
package org.fabric3.spi.model.physical;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalComponent;

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
     * Generates physical component definitions for the logical components.
     * 
     * @param components Set of logical components for which physical components 
     * need to be generated.
     * @return Map of generator contexts. Each context in the map contains a physical 
     * change set and command set mapped to the runtime id to which the component is 
     * provisioned.
     */
    Map<URI, GeneratorContext> generate(Collection<LogicalComponent<?>> components) throws ActivateException;
    
    /**
     * Initializes the physical model generator.
     */
    void initialize();

}
