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
package org.fabric3.fabric.component.instancefactory.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.fabric.component.InstanceFactoryProvider;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilder;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderException;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderRegistry;
import org.fabric3.spi.model.physical.InstanceFactoryProviderDefinition;

/**
 * Default implementation of the registry.
 * 
 * @version $Revison$ $Date$
 */
public class DefaultIFProviderBuilderRegistry implements IFProviderBuilderRegistry {

    // Internal cache
    private Map<Class<?>, IFProviderBuilder<? extends InstanceFactoryProvider,
            ? extends InstanceFactoryProviderDefinition>> registry =
        new ConcurrentHashMap<Class<?>, IFProviderBuilder<? extends InstanceFactoryProvider,
                ? extends InstanceFactoryProviderDefinition>>();

    /**
     * Builds an instnace factory provider from a definition.
     * 
     * @param providerDefinition Provider definition.
     * @param cl Clasloader to use.
     * @return Instance factory provider.
     */
    @SuppressWarnings("unchecked")
    public InstanceFactoryProvider build(InstanceFactoryProviderDefinition providerDefinition, ClassLoader cl)
        throws IFProviderBuilderException {

        Class<? extends InstanceFactoryProviderDefinition> type = providerDefinition.getClass();
        IFProviderBuilder builder = registry.get(type);
        if(builder == null) {
            throw new NoRegisteredIFBuilderException(type.toString());
        }
        return builder.build(providerDefinition, cl);
    }

    /**
     * Registers the builder.
     */
    public <IFPD extends InstanceFactoryProviderDefinition> void register(Class<IFPD> ifpdClass,
                                                                          IFProviderBuilder<?, IFPD> builder) {
        registry.put(ifpdClass, builder);
    }

}
