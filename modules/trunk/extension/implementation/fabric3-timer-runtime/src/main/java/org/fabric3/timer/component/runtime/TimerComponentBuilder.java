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
package org.fabric3.timer.component.runtime;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.implementation.PojoComponentBuilder;
import org.fabric3.pojo.implementation.PojoComponentContext;
import org.fabric3.pojo.implementation.PojoRequestContext;
import org.fabric3.pojo.injection.ConversationIDObjectFactory;
import org.fabric3.pojo.injection.MultiplicityObjectFactory;
import org.fabric3.pojo.instancefactory.InstanceFactoryBuilderRegistry;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.timer.component.provision.TimerComponentDefinition;
import org.fabric3.timer.component.provision.TriggerData;
import org.fabric3.timer.spi.TimerService;
import org.fabric3.transform.PullTransformer;
import org.fabric3.transform.TransformerRegistry;

/**
 * @version $Revision$ $Date$
 */
@EagerInit
public class TimerComponentBuilder<T> extends PojoComponentBuilder<T, TimerComponentDefinition, TimerComponent<?>> {
    private TimerService nonTrxTimerService;
    private TimerService trxTimerService;
    private ProxyService proxyService;

    public TimerComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                 @Reference ScopeRegistry scopeRegistry,
                                 @Reference InstanceFactoryBuilderRegistry providerBuilders,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry,
                                 @Reference ProxyService proxyService,
                                 @Reference(name = "nonTrxTimerService")TimerService nonTrxTimerService,
                                 @Reference(name = "trxTimerService")TimerService trxTimerService) {
        super(builderRegistry, scopeRegistry, providerBuilders, classLoaderRegistry, transformerRegistry);
        this.proxyService = proxyService;
        this.nonTrxTimerService = nonTrxTimerService;
        this.trxTimerService = trxTimerService;
    }

    @Init
    public void init() {
        builderRegistry.register(TimerComponentDefinition.class, this);
    }

    public TimerComponent<T> build(TimerComponentDefinition definition) throws BuilderException {
        URI componentId = definition.getComponentId();
        int initLevel = definition.getInitLevel();
        URI groupId = definition.getGroupId();
        ClassLoader classLoader = classLoaderRegistry.getClassLoader(definition.getClassLoaderId());

        // get the scope container for this component
        String scopeName = definition.getScope();
        Scope<?> scope = scopeRegistry.getScope(scopeName);
        ScopeContainer<?> scopeContainer = scopeRegistry.getScopeContainer(scope);

        // create the InstanceFactoryProvider based on the definition in the model
        InstanceFactoryDefinition providerDefinition = definition.getInstanceFactoryProviderDefinition();


        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        Map<String, ObjectFactory<?>> propertyFactories = createPropertyFactories(definition, provider);
        Map<String, MultiplicityObjectFactory<?>> referenceFactories = createMultiplicityReferenceFactories(providerDefinition);
        TriggerData data = definition.getTriggerData();
        TimerService timerService;
        if (definition.isTransactional()) {
            timerService = trxTimerService;
        } else {
            timerService = nonTrxTimerService;
        }
        TimerComponent<T> component = new TimerComponent<T>(componentId,
                                                            provider,
                                                            scopeContainer,
                                                            groupId,
                                                            initLevel,
                                                            definition.getMaxIdleTime(),
                                                            definition.getMaxAge(),
                                                            proxyService,
                                                            propertyFactories,
                                                            referenceFactories,
                                                            data,
                                                            timerService);

        PojoRequestContext requestContext = new PojoRequestContext();
        provider.setObjectFactory(InjectableAttribute.REQUEST_CONTEXT, new SingletonObjectFactory<PojoRequestContext>(requestContext));

        PojoComponentContext componentContext = new PojoComponentContext(component, requestContext);
        provider.setObjectFactory(InjectableAttribute.COMPONENT_CONTEXT, new SingletonObjectFactory<PojoComponentContext>(componentContext));
        provider.setObjectFactory(InjectableAttribute.CONVERSATION_ID, new ConversationIDObjectFactory());

        return component;

    }

}
