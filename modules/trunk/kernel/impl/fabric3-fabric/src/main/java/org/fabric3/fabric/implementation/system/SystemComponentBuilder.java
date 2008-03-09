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
package org.fabric3.fabric.implementation.system;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.implementation.PojoComponentBuilder;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;

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
            @Reference TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
            @Reference EventService eventService) {
        super(builderRegistry,
              scopeRegistry,
              providerBuilders,
              classLoaderRegistry,
              transformerRegistry,
              eventService);
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
        Scope<?> scope = definition.getScope();
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(scope);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition providerDefinition = definition.getInstanceFactoryProviderDefinition();
        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        createPropertyFactories(definition, provider);
        Map<String, MultiplicityObjectFactory<?>> referenceFactories = createMultiplicityReferenceFactories(providerDefinition);

        return new SystemComponent<T>(componentId, provider, scopeContainer, groupId, initLevel, -1, -1, referenceFactories);
    }
}
