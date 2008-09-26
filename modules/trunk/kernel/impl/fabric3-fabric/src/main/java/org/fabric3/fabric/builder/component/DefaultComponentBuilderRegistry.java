/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.builder.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.fabric.builder.BuilderNotFoundException;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Default map-based implementation of the component builder registry.
 * <p/>
 *
 * @version $Rev$ $Date$
 */
public class DefaultComponentBuilderRegistry implements ComponentBuilderRegistry {

    // Internal cache
    private Map<Class<?>,
            ComponentBuilder<? extends PhysicalComponentDefinition, ? extends Component>> registry =
            new ConcurrentHashMap<Class<?>,
                    ComponentBuilder<? extends PhysicalComponentDefinition, ? extends Component>>();

    /**
     * Registers a physical component builder.
     *
     * @param <PCD>           Type of the physical component definition.
     * @param definitionClass Class of the physical component definition.
     * @param builder         Builder for the physical component definition.
     */
    public <PCD extends PhysicalComponentDefinition,
            C extends Component> void register(Class<?> definitionClass, ComponentBuilder<PCD, C> builder) {
        registry.put(definitionClass, builder);
    }

    /**
     * Builds a physical component from component definition.
     *
     * @param componentDefinition Component definition.
     * @return Component to be built.
     */
    @SuppressWarnings("unchecked")
    public Component build(PhysicalComponentDefinition componentDefinition) throws BuilderException {

        ComponentBuilder builder = registry.get(componentDefinition.getClass());
        if (builder == null) {
            throw new BuilderNotFoundException("Builder not found for " + componentDefinition.getClass().getName());
        }
        return builder.build(componentDefinition);

    }

}
