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
package org.fabric3.fabric.wire.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.net.URI;

import org.osoa.sca.CallableReference;
import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyCreationException;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.fabric.wire.NoMethodForOperationException;
import org.fabric3.fabric.wire.WireObjectFactory;
import org.fabric3.fabric.injection.CallbackWireObjectFactory;

/**
 * the default implementation of a wire service that uses JDK dynamic proxies
 *
 * @version $$Rev$$ $$Date$$
 */
public class JDKProxyService implements ProxyService {
    private final ScopeRegistry scopeRegistry;
    private final ClassLoaderRegistry classLoaderRegistry;

    public JDKProxyService(@Reference ScopeRegistry scopeRegistry, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.scopeRegistry = scopeRegistry;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, boolean conversational, Wire wire, String callbackUri)
            throws ProxyCreationException {
        Map<Method, InvocationChain> mappings = createInterfaceToWireMapping(interfaze, wire);
        return new WireObjectFactory<T>(interfaze, conversational, callbackUri, this, mappings);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, Scope sourceScope, boolean conversaitonal, URI targetUri, Wire wire)
            throws ProxyCreationException {
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        Map<String, Map<Method, InvocationChain>> mappings = new HashMap<String, Map<Method, InvocationChain>>();
        mappings.put(targetUri.toString(), operationMappings);
        return new CallbackWireObjectFactory<T>(interfaze, sourceScope, conversaitonal, this, mappings);
    }

    public <T> T createProxy(Class<T> interfaze, boolean conversational, String callbackUri, Map<Method, InvocationChain> mappings)
            throws ProxyCreationException {
        ScopeContainer<Conversation> scopeContainer = scopeRegistry.getScopeContainer(Scope.CONVERSATION);
        JDKInvocationHandler<T> handler =
                new JDKInvocationHandler<T>(interfaze, callbackUri, conversational, mappings, scopeContainer);
        return handler.getService();
    }

    public <T> T createCallbackProxy(Class<T> interfaze, boolean conversational, Map<String, Map<Method, InvocationChain>> mappings)
            throws ProxyCreationException {
        ClassLoader cl = interfaze.getClassLoader();
        MultiThreadedCallbackInvocationHandler<T> handler = new MultiThreadedCallbackInvocationHandler<T>(interfaze, conversational, mappings);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    public <T> T createStatefullCallbackProxy(Class<T> interfaze, boolean conversational, String callbackUri, Map<Method, InvocationChain> mapping) {
        ClassLoader cl = interfaze.getClassLoader();
        StatefullCallbackInvocationHandler<T> handler = new StatefullCallbackInvocationHandler<T>(interfaze, conversational, callbackUri, mapping);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        InvocationHandler handler = Proxy.getInvocationHandler(target);
        if (handler instanceof JDKInvocationHandler) {
            JDKInvocationHandler<B> jdkHandler = (JDKInvocationHandler<B>) handler;
            return (R) jdkHandler.getServiceReference();
        } else if (handler instanceof MultiThreadedCallbackInvocationHandler) {
            // TODO return a CallbackReference
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("Not a Fabric3 SCA proxy");
        }
    }

    private Map<Method, InvocationChain> createInterfaceToWireMapping(Class<?> interfaze, Wire wire) throws NoMethodForOperationException {

        Map<PhysicalOperationDefinition, InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<Method, InvocationChain>(invocationChains.size());
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : invocationChains.entrySet()) {
            PhysicalOperationDefinition operation = entry.getKey();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, entry.getValue());
            } catch (NoSuchMethodException e) {
                throw new NoMethodForOperationException(operation.getName());
            } catch (ClassNotFoundException e) {
                throw new ProxyCreationException(e);
            }
        }
        return chains;
    }

    /**
     * Returns the matching method from the class for a given operation.
     *
     * @param clazz     the class to introspect
     * @param operation the operation to match
     * @return a matching method
     * @throws NoSuchMethodException  if a matching method is not found
     * @throws ClassNotFoundException if a parameter type specified in the operation is not found
     */
    private Method findMethod(Class<?> clazz, PhysicalOperationDefinition operation) throws NoSuchMethodException, ClassNotFoundException {
        String name = operation.getName();
        List<String> params = operation.getParameters();
        Class<?>[] types = new Class<?>[params.size()];
        for (int i = 0; i < params.size(); i++) {
            types[i] = classLoaderRegistry.loadClass(clazz.getClassLoader(), params.get(i));
        }
        return clazz.getMethod(name, types);
    }
}
