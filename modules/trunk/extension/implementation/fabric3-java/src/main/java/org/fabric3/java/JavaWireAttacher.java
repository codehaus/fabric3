/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.java;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.pojo.wire.PojoWireAttacher;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.instance.ValueSource;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;
import org.fabric3.spi.transform.TransformerRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a
 * physical component definition
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JavaWireAttacher extends PojoWireAttacher implements SourceWireAttacher<JavaWireSourceDefinition>, TargetWireAttacher<JavaWireTargetDefinition> {

    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ComponentManager manager;
    private final ProxyService proxyService;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JavaWireAttacher(@Reference ComponentManager manager,
                            @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                            @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                            @Reference ProxyService proxyService,
                            @Reference ClassLoaderRegistry classLoaderRegistry,
                            @Reference(name = "transformerRegistry")TransformerRegistry<PullTransformer<?, ?>> transformerRegistry) {
        super(transformerRegistry, classLoaderRegistry);
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.manager = manager;
        this.proxyService = proxyService;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(JavaWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(JavaWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(JavaWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(JavaWireTargetDefinition.class, this);
    }

    public void attachToSource(JavaWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {

        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component component = manager.getComponent(sourceName);
        assert component instanceof JavaComponent;
        JavaComponent<?> source = (JavaComponent) component;
        ValueSource referenceSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, sourceUri.getFragment());

        Class<?> type = source.getMemberType(referenceSource);
        URI targetUri = targetDefinition.getUri();

        Component target = null;
        if (targetUri != null) {
            URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
            target = manager.getComponent(targetName);
        }

        Object key = getKey(sourceDefinition, source, referenceSource);

        ObjectFactory<?> factory = createWireObjectFactory(type, sourceDefinition.isConversational(), wire);
        if (target != null) {
            source.attachReferenceToTarget(referenceSource, factory, key);
        } else {
            source.setObjectFactory(referenceSource, factory);
        }
        if (!wire.getCallbackInvocationChains().isEmpty()) {
            URI callbackUri = sourceDefinition.getCallbackUri();
            ValueSource callbackSource =
                    new ValueSource(ValueSource.ValueSourceType.SERVICE, callbackUri.getFragment());
            Class<?> callbackType = source.getMemberType(callbackSource);
            source.setObjectFactory(callbackSource, createCallbackWireObjectFactory(callbackType));
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               JavaWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
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
                    paramTypes[i] = classLoaderRegistry.loadClass(loader, param);
                } catch (ClassNotFoundException e) {
                    URI sourceUri = sourceDefinition.getUri();
                    URI targetUri = targetDefinition.getUri();
                    throw new WireAttachException("Implementation class not found", sourceUri, targetUri, e);
                }
            }
            Method method;
            try {
                method = implementationClass.getMethod(operation.getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                URI sourceUri = sourceDefinition.getUri();
                URI targetUri = targetDefinition.getUri();
                throw new WireAttachException("No matching method found", sourceUri, targetUri, e);
            }
            chain.addInterceptor(createInterceptor(method, target, scopeContainer));
        }
    }

    public void attachObjectFactory(JavaWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        JavaComponent<?> sourceComponent = (JavaComponent<?>) manager.getComponent(sourceId);
        ValueSource referenceSource = new ValueSource(ValueSource.ValueSourceType.REFERENCE, source.getUri().getFragment());

        Object key = getKey(source, sourceComponent, referenceSource);
        sourceComponent.attachReferenceToTarget(referenceSource, objectFactory, key);
    }

    public ObjectFactory<?> createObjectFactory(JavaWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        JavaComponent<?> targetComponent = (JavaComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }

    private <T, CONTEXT> InvokerInterceptor<T, CONTEXT> createInterceptor(Method method,
                                                                          JavaComponent<T> component,
                                                                          ScopeContainer<CONTEXT> scopeContainer) {
        return new InvokerInterceptor<T, CONTEXT>(method, component, scopeContainer);
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, boolean isConversational, Wire wire) {
        return proxyService.createObjectFactory(type, isConversational, wire);
    }

    private <T> ObjectFactory<T> createCallbackWireObjectFactory(Class<T> type) {
        throw new UnsupportedOperationException();
    }
}
