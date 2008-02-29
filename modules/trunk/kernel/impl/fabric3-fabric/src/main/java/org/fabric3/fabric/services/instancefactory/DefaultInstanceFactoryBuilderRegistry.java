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
package org.fabric3.fabric.services.instancefactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderException;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;

/**
 * Default implementation of the registry.
 *
 * @version $Revison$ $Date$
 */
public class DefaultInstanceFactoryBuilderRegistry implements InstanceFactoryBuilderRegistry {

    // Internal cache
    private Map<Class<?>, InstanceFactoryBuilder<? extends InstanceFactoryProvider,
            ? extends InstanceFactoryDefinition>> registry =
            new ConcurrentHashMap<Class<?>, InstanceFactoryBuilder<? extends InstanceFactoryProvider,
                    ? extends InstanceFactoryDefinition>>();

    /**
     * Builds an instnace factory provider from a definition.
     *
     * @param providerDefinition Provider definition.
     * @param cl                 Clasloader to use.
     * @return Instance factory provider.
     */
    @SuppressWarnings("unchecked")
    public InstanceFactoryProvider build(InstanceFactoryDefinition providerDefinition, ClassLoader cl) throws InstanceFactoryBuilderException {
        Class<? extends InstanceFactoryDefinition> type = providerDefinition.getClass();
        InstanceFactoryBuilder builder = registry.get(type);
        if (builder == null) {
            String id = type.toString();
            throw new NoRegisteredIFBuilderException("No registered builder for: " + id, id);
        }
        return builder.build(providerDefinition, cl);
    }

    /**
     * Registers the builder.
     */
    public <IFPD extends InstanceFactoryDefinition> void register(Class<?> ifpdClass, InstanceFactoryBuilder<?, IFPD> builder) {
        registry.put(ifpdClass, builder);
    }

}
