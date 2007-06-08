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

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.component.InstanceFactoryProvider;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderRegistry;
import org.fabric3.fabric.implementation.pojo.PojoComponentBuilder;
import org.fabric3.fabric.injection.CallbackWireObjectFactory;
import org.fabric3.fabric.wire.WireObjectFactory;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.builder.component.ComponentBuilderRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.instance.ValueSource;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.REFERENCE;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.SERVICE;
import org.fabric3.spi.model.physical.InstanceFactoryProviderDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the defined component
 */
@EagerInit
@Service(interfaces = {ComponentBuilder.class, WireAttacher.class})
public class JavaComponentBuilder<T> extends PojoComponentBuilder<T, JavaComponentDefinition, JavaComponent<T>>
        implements WireAttacher<JavaWireSourceDefinition, JavaWireTargetDefinition> {

    private ProxyService proxyService;

    public JavaComponentBuilder(@Reference ComponentBuilderRegistry builderRegistry,
                                @Reference ComponentManager manager,
                                @Reference WireAttacherRegistry wireAttacherRegistry,
                                @Reference ScopeRegistry scopeRegistry,
                                @Reference IFProviderBuilderRegistry providerBuilders,
                                @Reference ClassLoaderRegistry classLoaderRegistry,
                                @Reference TransformerRegistry<PullTransformer<?,?>> transformerRegistry) {
        super(builderRegistry,
              manager,
              wireAttacherRegistry,
              scopeRegistry,
              providerBuilders,
              classLoaderRegistry,
              transformerRegistry);
    }

    @Reference
    public void setProxyService(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @Init
    public void init() {
        builderRegistry.register(JavaComponentDefinition.class, this);
        wireAttacherRegistry.register(JavaWireSourceDefinition.class, this);
        wireAttacherRegistry.register(JavaWireTargetDefinition.class, this);
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
        InstanceFactoryProviderDefinition providerDefinition = definition.getInstanceFactoryProviderDefinition();
        InstanceFactoryProvider<T> provider = providerBuilders.build(providerDefinition, classLoader);

        Map<String, ObjectFactory<?>> propertyFactories = createPropertyFactories(definition, provider);

        return new JavaComponent<T>(componentId, provider, scopeContainer, groupId, initLevel, -1, -1, proxyService,
                                    propertyFactories);
    }

    public void attachToSource(JavaWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {
        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component component = manager.getComponent(sourceName);
        assert component instanceof JavaComponent;
        JavaComponent<?> source = (JavaComponent) component;
        ValueSource referenceSource = new ValueSource(REFERENCE, sourceUri.getFragment());

        Class<?> type = source.getMemberType(referenceSource);
        if (sourceDefinition.isOptimizable()) {
            URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
            Component target = manager.getComponent(targetName);
            assert target instanceof AtomicComponent;
            ObjectFactory<?> factory = ((AtomicComponent<?>) target).createObjectFactory();
            source.setObjectFactory(referenceSource, factory);
        } else {
            ObjectFactory<?> factory = createWireObjectFactory(type, sourceDefinition.isConversational(), wire);
            source.setObjectFactory(referenceSource, factory);
            if (!wire.getCallbackInvocationChains().isEmpty()) {
                URI callbackUri = sourceDefinition.getCallbackUri();
                ValueSource callbackSource = new ValueSource(SERVICE, callbackUri.getFragment());
                Class<?> callbackType = source.getMemberType(callbackSource);
                source.setObjectFactory(callbackSource, createCallbackWireObjectFactory(callbackType));
            }
        }
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, boolean isConversational, Wire wire) {
        return new WireObjectFactory<T>(type, isConversational, wire, proxyService);
    }

    private <T> ObjectFactory<T> createCallbackWireObjectFactory(Class<T> type) {
        return new CallbackWireObjectFactory<T>(type, proxyService);
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               JavaWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
        if (sourceDefinition.isOptimizable()) {
            return;
        }
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof JavaComponent;
        JavaComponent<?> target = (JavaComponent) component;

        ScopeContainer<?> scopeContainer = target.getScopeContainer();
        Class<?> implementationClass = target.getImplementationClass();
        ClassLoader loader = implementationClass.getClassLoader();
        // attach the invoker interceptor to forward invocation chains
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();
            InvocationChain chain = entry.getValue();
            List<String> params = operation.getParameters();
            Class<?>[] paramTypes = new Class<?>[params.size()];
            assert loader != null;
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                try {
                    paramTypes[i] = loader.loadClass(param);
                } catch (ClassNotFoundException e) {
                    URI sourceUri = wire.getSourceUri();
                    URI targetUri = wire.getTargetUri();
                    throw new WireAttachException("Implementation class not found", sourceUri, targetUri, e);
                }
            }
            Method method;
            try {
                method = implementationClass.getMethod(operation.getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                URI sourceUri = wire.getSourceUri();
                URI targetUri = wire.getTargetUri();
                throw new WireAttachException("No matching method found", sourceUri, targetUri, e);
            }
            chain.addInterceptor(createInterceptor(method, target, scopeContainer));
        }
    }

    private <T, CONTEXT> JavaInvokerInterceptor<T, CONTEXT> createInterceptor(Method method,
                                                                              JavaComponent<T> component,
                                                                              ScopeContainer<CONTEXT> scopeContainer) {
        return new JavaInvokerInterceptor<T, CONTEXT>(method, component, scopeContainer);
    }
}
