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
package org.fabric3.system.runtime;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.builder.PojoComponentBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.provision.InstanceFactoryDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.system.provision.SystemComponentDefinition;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class SystemComponentBuilder<T> extends PojoComponentBuilder<T, SystemComponentDefinition, SystemComponent<T>> {

    public SystemComponentBuilder(
            @Reference ComponentBuilderRegistry builderRegistry,
            @Reference ScopeRegistry scopeRegistry,
            @Reference InstanceFactoryBuilderRegistry providerBuilders,
            @Reference ClassLoaderRegistry classLoaderRegistry,
            @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(builderRegistry, scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
    }

    @Init
    public void init() {
        builderRegistry.register(SystemComponentDefinition.class, this);
    }

    public SystemComponent<T> build(SystemComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        int initLevel = definition.getInitLevel();
        URI groupId = definition.getGroupId();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(Scope.COMPOSITE);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition providerDefinition = definition.getProviderDefinition();
        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        createPropertyFactories(definition, provider);

        return new SystemComponent<T>(componentId, provider, scopeContainer, groupId, initLevel, -1, -1);
    }
}
