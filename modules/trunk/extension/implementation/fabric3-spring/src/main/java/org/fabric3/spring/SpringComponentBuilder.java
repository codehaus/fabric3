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
package org.fabric3.spring;

import org.fabric3.pojo.implementation.PojoComponentBuilder;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spring.applicationContext.SCAApplicationContext;
import org.fabric3.spring.applicationContext.SCAParentApplicationContext;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * The component builder for Spring implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the defined component
 */
@EagerInit
public class SpringComponentBuilder<T> extends PojoComponentBuilder<T, SpringComponentDefinition, SpringComponent<T>> {

    public SpringComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ScopeRegistry scopeRegistry,
                                @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference(name = "transformerRegistry") TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                @Reference ProxyService proxyService) {
        super(builderRegistry, scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
    }

    @Init
    public void init() {
        builderRegistry.register(SpringComponentDefinition.class, this);
    }

    public SpringComponent<T> build(SpringComponentDefinition componentDefinition) throws BuilderException {
        String springBeanId = componentDefinition.getSpringBeanId();

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(componentDefinition.getClassLoaderId());


        SCAParentApplicationContext parentAC =
            new SCAParentApplicationContext(classLoader, componentDefinition);
        SCAApplicationContext scaApplicationContext =
            new SCAApplicationContext(parentAC, componentDefinition.getResource(), classLoader);
        
        ObjectFactory<T> objectFactory = new SpringObjectFactory<T>(scaApplicationContext, springBeanId);

        SpringComponent<T> springComponent = new SpringComponent<T>(componentDefinition.getComponentId(), objectFactory);
        
        parentAC.setSpringComponent(springComponent);
        
        return springComponent;

    }
}
