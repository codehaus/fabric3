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

import java.net.URI;

import org.fabric3.spi.model.type.ResourceDefinition;

/**
 * Implementations generate physical resource definitions
 *
 * @version $Rev$ $Date$
 */
public interface ResourceGenerator {

    /**
     * Updates the change set with a physical resource definition
     *
     * @param definition the logical resource definition to generate the physical definition from
     * @param context    the current generator context
     * @return the id of the physical resource
     * @throws GenerationException if an error occurs during the generation process
     */
    URI generate(ResourceDefinition definition, GeneratorContext context) throws GenerationException;

}
