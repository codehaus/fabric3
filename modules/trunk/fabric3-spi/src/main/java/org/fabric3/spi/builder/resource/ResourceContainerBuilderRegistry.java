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
package org.fabric3.spi.builder.resource;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;
import org.fabric3.spi.resource.ResourceContainer;

/**
 * A registry of resource container builders
 *
 * @version $Rev$ $Date$
 */
public interface ResourceContainerBuilderRegistry {

    /**
     * Registers the builder for the given type
     *
     * @param clazz   the PhysicalResourceContainerDefinition type the builder handles
     * @param builder the builder
     */
    <T extends PhysicalResourceContainerDefinition> void register(Class<T> clazz, ResourceContainerBuilder<T> builder);

    /**
     * Dispatches to a builder to create a resource container
     *
     * @param parent     the parent container or null
     * @param definition the container definition to build from
     * @throws BuilderException if an error occurs during build
     */
    void build(ResourceContainer parent, PhysicalResourceContainerDefinition definition) throws BuilderException;

}
