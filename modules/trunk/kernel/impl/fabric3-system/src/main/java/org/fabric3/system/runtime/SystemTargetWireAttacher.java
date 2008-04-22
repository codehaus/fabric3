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
package org.fabric3.system.runtime;

import java.net.URI;
import java.util.Map;
import java.util.List;
import java.lang.reflect.Method;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.runtime.component.ComponentManager;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.system.provision.SystemWireTargetDefinition;

/**
 * @version $Rev$ $Date$
 */
public class SystemTargetWireAttacher implements TargetWireAttacher<SystemWireTargetDefinition> {

    private final ComponentManager manager;
    private final ClassLoaderRegistry classLoaderRegistry;

    public SystemTargetWireAttacher(@Reference ComponentManager manager,
                                    @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.manager = manager;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, SystemWireTargetDefinition target, Wire wire) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SystemComponent<?> targetComponent = (SystemComponent<?>) manager.getComponent(targetId);

        ScopeContainer<?> scopeContainer = targetComponent.getScopeContainer();
        Class<?> implementationClass = targetComponent.getImplementationClass();
        ClassLoader loader = implementationClass.getClassLoader();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();
            InvocationChain chain = entry.getValue();

            List<String> params = operation.getParameters();
            Class<?>[] paramTypes = new Class<?>[params.size()];
            for (int i = 0; i < params.size(); i++) {
                String param = params.get(i);
                try {
                    paramTypes[i] = classLoaderRegistry.loadClass(loader, param);
                } catch (ClassNotFoundException e) {
                    URI sourceUri = source.getUri();
                    URI targetUri = target.getUri();
                    throw new WireAttachException("Implementation class not found", sourceUri, targetUri, e);
                }
            }
            Method method;
            try {
                method = implementationClass.getMethod(operation.getName(), paramTypes);
            } catch (NoSuchMethodException e) {
                URI sourceUri = source.getUri();
                URI targetUri = target.getUri();
                throw new WireAttachException("No matching method found", sourceUri, targetUri, e);
            }

            chain.addInterceptor(createInterceptor(method, targetComponent, scopeContainer));
        }
    }

    <T> SystemInvokerInterceptor<T> createInterceptor(Method method, SystemComponent<T> component, ScopeContainer<?> scopeContainer) {
        return new SystemInvokerInterceptor<T>(method, scopeContainer, component);
    }

    public ObjectFactory<?> createObjectFactory(SystemWireTargetDefinition target) throws WiringException {
        URI targetId = UriHelper.getDefragmentedName(target.getUri());
        SystemComponent<?> targetComponent = (SystemComponent<?>) manager.getComponent(targetId);
        return targetComponent.createObjectFactory();
    }
}