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
package org.fabric3.java.runtime;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.provision.JavaWireTargetDefinition;
import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches wires to and from components implemented using the Java programming model.
 *
 * @version $Rev$ $Date$
 */
public class JavaTargetWireAttacher implements TargetWireAttacher<JavaWireTargetDefinition> {

    private final ComponentManager manager;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JavaTargetWireAttacher(@Reference ComponentManager manager,
                                  @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, JavaWireTargetDefinition targetDefinition, Wire wire)
            throws WireAttachException {
        URI targetName = UriHelper.getDefragmentedName(targetDefinition.getUri());
        Component component = manager.getComponent(targetName);
        assert component instanceof JavaComponent;

        JavaComponent<?> target = (JavaComponent<?>) component;

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
            boolean endsConversation = operation.isEndsConversation();
            boolean callback = targetDefinition.isCallback();
            if (callback) {
                // callbacks do not expire the client (i.e. the callback target); they expire the forward implementation instance
                endsConversation = false;
            }
            InvokerInterceptor<?, ?> interceptor = createInterceptor(method, callback, endsConversation, target, scopeContainer);
            chain.addInterceptor(interceptor);
        }
    }

    <T, CONTEXT> InvokerInterceptor<T, CONTEXT> createInterceptor(Method method,
                                                                  boolean callback,
                                                                  boolean endsConvesation,
                                                                  AtomicComponent<T> component,
                                                                  ScopeContainer<CONTEXT> scopeContainer) {
        return new InvokerInterceptor<T, CONTEXT>(method, callback, endsConvesation, component, scopeContainer);
    }

    public ObjectFactory<?> createObjectFactory(JavaWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        JavaComponent<?> targetComponent = (JavaComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}