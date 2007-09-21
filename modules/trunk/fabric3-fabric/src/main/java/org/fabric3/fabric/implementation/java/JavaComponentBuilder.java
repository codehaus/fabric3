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
package org.fabric3.fabric.implementation.java;

import java.net.URI;
import java.util.Map;

import org.fabric3.pojo.implementation.PojoComponentBuilder;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.wire.ProxyService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the defined component
 */
@EagerInit
public class JavaComponentBuilder<T> extends PojoComponentBuilder<T, JavaComponentDefinition, JavaComponent<T>> {

    private ProxyService proxyService;

    public JavaComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ScopeRegistry scopeRegistry,
                                @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference(name = "transformerRegistry")
                                TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                @Reference ProxyService proxyService) {
        super(builderRegistry,
              scopeRegistry,
              providerBuilders,
              classLoaderRegistry,
              transformerRegistry);
        this.proxyService = proxyService;
    }

    @Init
    public void init() {
        builderRegistry.register(JavaComponentDefinition.class, this);
    }

    public JavaComponent<T> build(JavaComponentDefinition definition) throws BuilderException {
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

        Map<String, ObjectFactory<?>> propertyFactories = createPropertyFactories(definition, provider);
        Map<String, MultiplicityObjectFactory<?>> referenceFactories =
                createMultiplicityReferenceFactories(providerDefinition);

        return new JavaComponent<T>(componentId, provider, scopeContainer, groupId, initLevel, -1, -1, proxyService,
                                    propertyFactories, referenceFactories);

    }
}
