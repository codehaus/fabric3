/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasisopen.sca.ServiceReference;
import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.model.type.component.Scope;
import org.fabric3.pojo.builder.ProxyCreationException;
import org.fabric3.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * the default implementation of a wire service that uses JDK dynamic proxies
 *
 * @version $$Rev: 3150 $$ $$Date: 2008-03-21 14:12:51 -0700 (Fri, 21 Mar 2008) $$
 */
public class JDKProxyService implements ProxyService {
    private ClassLoaderRegistry classLoaderRegistry;
    private ScopeRegistry scopeRegistry;
    private ScopeContainer conversationalContainer;

    public JDKProxyService() {
    }

    @Constructor
    public JDKProxyService(@Reference ClassLoaderRegistry classLoaderRegistry, @Reference ScopeRegistry scopeRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
        this.scopeRegistry = scopeRegistry;
    }

    public <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, InteractionType type, Wire wire, String callbackUri)
            throws ProxyCreationException {
        Map<Method, InvocationChain> mappings = createInterfaceToWireMapping(interfaze, wire);
        return new WireObjectFactory<T>(interfaze, type, callbackUri, this, mappings);
    }

    public <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, ScopeContainer container, URI callbackUri, Wire wire)
            throws ProxyCreationException {
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        Map<String, Map<Method, InvocationChain>> mappings = new HashMap<String, Map<Method, InvocationChain>>();
        mappings.put(callbackUri.toString(), operationMappings);
        return new CallbackWireObjectFactory<T>(interfaze, container, this, mappings);
    }

    public <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory,
                                                            Class<T> interfaze,
                                                            ScopeContainer container,
                                                            URI callbackUri,
                                                            Wire wire) throws ProxyCreationException {
        if (!(factory instanceof CallbackWireObjectFactory)) {
            // a placeholder object factory (i.e. created when the callback is not wired) needs to be replaced 
            return createCallbackObjectFactory(interfaze, container, callbackUri, wire);
        }
        CallbackWireObjectFactory<?> callbackFactory = (CallbackWireObjectFactory) factory;
        Map<Method, InvocationChain> operationMappings = createInterfaceToWireMapping(interfaze, wire);
        callbackFactory.updateMappings(callbackUri.toString(), operationMappings);
        return callbackFactory;
    }

    public <T> T createProxy(Class<T> interfaze, InteractionType type, String callbackUri, Map<Method, InvocationChain> mappings)
            throws ProxyCreationException {
        JDKInvocationHandler<T> handler;
        if (InteractionType.CONVERSATIONAL == type || InteractionType.PROPAGATES_CONVERSATION == type) {
            // create a conversational proxy
            ScopeContainer scopeContainer = getContainer();
            handler = new JDKInvocationHandler<T>(interfaze, type, callbackUri, mappings, scopeContainer);
        } else {
            // create a non-conversational proxy
            handler = new JDKInvocationHandler<T>(interfaze, callbackUri, mappings);
        }
        return handler.getService();
    }

    public <T> T createCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) throws ProxyCreationException {
        ClassLoader cl = interfaze.getClassLoader();
        MultiThreadedCallbackInvocationHandler<T> handler = new MultiThreadedCallbackInvocationHandler<T>(interfaze, mappings);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    public <T> T createStatefullCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping, ScopeContainer container) {
        ClassLoader cl = interfaze.getClassLoader();
        StatefulCallbackInvocationHandler<T> handler = new StatefulCallbackInvocationHandler<T>(interfaze, container, mapping);
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    @SuppressWarnings("unchecked")
    public <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException {
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

        List<InvocationChain> invocationChains = wire.getInvocationChains();

        Map<Method, InvocationChain> chains = new HashMap<Method, InvocationChain>(invocationChains.size());
        for (InvocationChain chain : invocationChains) {
            PhysicalOperationDefinition operation = chain.getPhysicalOperation();
            try {
                Method method = findMethod(interfaze, operation);
                chains.put(method, chain);
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

    private ScopeContainer getContainer() {
        if (conversationalContainer == null) {
            conversationalContainer = scopeRegistry.getScopeContainer(Scope.CONVERSATION);
        }
        return conversationalContainer;
    }
}
