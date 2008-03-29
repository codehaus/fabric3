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

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;

/**
 * Implementations generate physical resource definitions for components and bindings.
 *
 * @version $Rev$ $Date$
 */
public interface ResourceGenerator {

    /**
     * Updates the change set with a physical resource definition for the logical component. If the resource definition
     * already exits, it will be updated with resources required by the component.
     *
     * @param component the logical component to generate the physical definition from
     * @return the id of the physical resource
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalResourceContainerDefinition generate(LogicalComponent<?> component) throws GenerationException;

    /**
     * Updates the change set with a physical resource definition for the logical binding. If the resource definition
     * already exits, it will be updated with resources required by the binding.
     *
     * @param binding the logical binding to generate the physical definition from
     * @return the id of the physical resource
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalResourceContainerDefinition generate(LogicalBinding<?> binding) throws GenerationException;

    /**
     * Updates the change set with a physical resource definition for the logical resource. If the resource definition
     * already exits, it will be updated with resources required by the binding.
     *
     * @param resource the logical resource to generate the physical definition from
     * @return the id of the physical resource
     * @throws GenerationException if an error occurs during the generation process
     */
    PhysicalResourceContainerDefinition generate(LogicalResource<?> resource) throws GenerationException;

}
