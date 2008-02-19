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
package org.fabric3.groovy;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.InjectableAttributeType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * The component builder for Java implementation types. Responsible for creating the Component runtime artifact from a physical component definition
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces={SourceWireAttacher.class, TargetWireAttacher.class})
public class GroovyWireAttacher implements SourceWireAttacher<GroovyWireSourceDefinition>, TargetWireAttacher<GroovyWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final ComponentManager manager;
    private final ProxyService proxyService;

    public GroovyWireAttacher(@Reference ComponentManager manager,
                              @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                              @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                              @Reference ProxyService proxyService) {
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.manager = manager;
        this.proxyService = proxyService;
    }

    @Init
    public void init() {
        sourceWireAttacherRegistry.register(GroovyWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(GroovyWireTargetDefinition.class, this);
    }

    @Destroy
    public void destroy() {
        sourceWireAttacherRegistry.unregister(GroovyWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(GroovyWireTargetDefinition.class, this);
    }

    public void attachToSource(GroovyWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) {
        URI sourceUri = sourceDefinition.getUri();
        URI sourceName = UriHelper.getDefragmentedName(sourceDefinition.getUri());
        Component component = manager.getComponent(sourceName);
        assert component instanceof GroovyComponent;
        GroovyComponent<?> source = (GroovyComponent) component;
        InjectableAttribute referenceSource = new InjectableAttribute(InjectableAttributeType.REFERENCE, sourceUri.getFragment());

        Class<?> type = source.getMemberType(referenceSource);
        ObjectFactory<?> factory = createWireObjectFactory(type, sourceDefinition.isConversational(), wire);
        source.setObjectFactory(referenceSource, factory);
    }

    private <T> ObjectFactory<T> createWireObjectFactory(Class<T> type, boolean isConversational, Wire wire) {
        return proxyService.createObjectFactory(type, isConversational, wire);
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               GroovyWireTargetDefinition targetDefinition,
                               Wire wire) throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof GroovyComponent;
        GroovyComponent<?> target = (GroovyComponent) component;

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

        // TODO handle callbacks
    }

    private <T, CONTEXT> InvokerInterceptor<T, CONTEXT> createInterceptor(Method method,
                                                                          GroovyComponent<T> component,
                                                                          ScopeContainer<CONTEXT> scopeContainer) {
        return new InvokerInterceptor<T, CONTEXT>(method, component, scopeContainer);
    }

    public void attachObjectFactory(GroovyWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        URI sourceId = UriHelper.getDefragmentedName(source.getUri());
        GroovyComponent<?> sourceComponent = (GroovyComponent<?>) manager.getComponent(sourceId);
        InjectableAttribute referenceSource = new InjectableAttribute(InjectableAttributeType.REFERENCE, source.getUri().getFragment());

        sourceComponent.attachReferenceToTarget(referenceSource, objectFactory, null);
    }

    public ObjectFactory<?> createObjectFactory(GroovyWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        GroovyComponent<?> targetComponent = (GroovyComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}
