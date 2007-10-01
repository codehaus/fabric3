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
package org.fabric3.java;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.fabric3.pojo.implementation.PojoComponent;
import org.fabric3.pojo.reflection.InvokerInterceptor;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttachException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.component.ComponentManager;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JavaMappedResourceWireAttacher implements WireAttacher<PhysicalWireSourceDefinition, JavaMappedResourceWireTargetDefinition> {
    
    private ComponentManager componentManager;
    private ClassLoaderRegistry classLoaderRegistry;
    
    /**
     * Injects the component manager.
     * @param componentManager Component manager to be injected.
     */
    @Reference
    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }
    
    /**
     * Injects the classLoader registry.
     * @param classLoaderRegistry ClassLoader registry to be injected.
     */
    @Reference
    public void setClassLoaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }
    
    /**
     * Registers with the registry.
     * @param registry Injects the registry.
     */
    @Reference
    public void setRegistry(WireAttacherRegistry registry) {
        registry.register(JavaMappedResourceWireTargetDefinition.class, this);
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(PhysicalWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        // No-op
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, JavaMappedResourceWireTargetDefinition targetDefinition, Wire wire) throws WiringException {


        Component component = componentManager.getComponent(targetDefinition.getUri());
        assert component instanceof PojoComponent;
        PojoComponent<?> target = (PojoComponent<?>) component;

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

    /*
     * Creates the interceptor.
     */
    private <T, CONTEXT> InvokerInterceptor<T, CONTEXT> createInterceptor(Method method,
                                                                          PojoComponent<T> component,
                                                                          ScopeContainer<CONTEXT> scopeContainer) {
        return new InvokerInterceptor<T, CONTEXT>(method, component, scopeContainer);
    }

}
