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
package org.fabric3.fabric.builder.resource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.builder.BuilderConfigException;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.resource.ResourceContainerBuilder;
import org.fabric3.spi.builder.resource.ResourceContainerBuilderRegistry;
import org.fabric3.spi.model.physical.PhysicalResourceContainerDefinition;
import org.fabric3.spi.resource.ResourceContainer;

/**
 * Default implementation of ResourceContainerBuilderRegistry
 *
 * @version $Rev$ $Date$
 */
public class ResourceContainerBuilderRegistryImpl implements ResourceContainerBuilderRegistry {
    private Map<Class<?>, ResourceContainerBuilder<?>> builders =
            new ConcurrentHashMap<Class<?>, ResourceContainerBuilder<?>>();

    public <T extends PhysicalResourceContainerDefinition> void register(Class<T> clazz,
                                                                         ResourceContainerBuilder<T> builder) {
        builders.put(clazz, builder);
    }

    @SuppressWarnings({"unchecked"})
    public void build(ResourceContainer parent, PhysicalResourceContainerDefinition definition)
            throws BuilderException {
        Class<? extends PhysicalResourceContainerDefinition> key = definition.getClass();
        ResourceContainerBuilder builder = builders.get(key);
        if (builder == null) {
            throw new BuilderConfigException("Builder not found for", key.getClass().getName());
        }
        builder.build(parent, definition);
    }
}
