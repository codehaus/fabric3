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
import org.fabric3.spi.model.physical.PhysicalClassLoaderDefinition;

/**
 * Generates classloader definitions for components being provisioned to a runtime. Components contained in the same composite that are provisioned to
 * a node will share the same classloader. Each composite in a hierarchy will have its own classloader with a parent set to the classloader of the
 * containing composite.
 * <p/>
 * The generator introspects the logical component for a {@link org.fabric3.spi.model.type.ContributionResourceDescription}.From this metadata it adds
 * the contribution that the artifacts for the component are containined in to the classloader definition. The classloader definition is then added to
 * the physical change set that will be sent to the service node.
 * <p/>
 * On the service node, a builder is responsible for matching the PhysicalClassLoaderDefinition to an existing classloader or creating a new one.
 * During this process, the contribution archive and required extensions may need to be provisioned to the node.
 * <p/>
 * Component extension generators requiring classloader provisioning can use this service to create the required classloader definition.
 *
 * @version $Rev$ $Date$
 */
public interface ClassLoaderGenerator {
    /**
     * Creates a classloader definition required to instantiate component. The definition may reference resources such as jars required by the
     * component implementation.
     *
     * @param component the logical component to generate the classloader definition from
     * @return the definition
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalClassLoaderDefinition generate(LogicalComponent<?> component) throws GenerationException;

}
