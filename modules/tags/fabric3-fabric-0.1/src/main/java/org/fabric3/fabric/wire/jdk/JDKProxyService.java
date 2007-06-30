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

import org.osoa.sca.CallableReference;
import org.osoa.sca.Conversation;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyCreationException;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * the default implementation of a wire service that uses JDK dynamic proxies
 *
 * @version $$Rev$$ $$Date$$
 */
public class JDKProxyService implements ProxyService {
    private final ScopeRegistry scopeRegistry;

    public JDKProxyService(@Reference ScopeRegistry scopeRegistry) {
        super();
        this.scopeRegistry = scopeRegistry;
    }

    public <T> T createProxy(Class<T> interfaze,
                             boolean conversational,
                             Wire wire,
                             Map<Method, InvocationChain> mappings) throws ProxyCreationException {
        assert interfaze != null;
        assert wire != null;
        ScopeContainer<Conversation> scopeContainer = scopeRegistry.getScopeContainer(Scope.CONVERSATION);
        JDKInvocationHandler<T> handler =
                new JDKInvocationHandler<T>(interfaze, wire, conversational, mappings, scopeContainer);
        return handler.getService();
    }

    public Object createCallbackProxy(Class<?> interfaze) throws ProxyCreationException {
        ClassLoader cl = interfaze.getClassLoader();
        JDKCallbackInvocationHandler handler = new JDKCallbackInvocationHandler();
        return interfaze.cast(Proxy.newProxyInstance(cl, new Class[]{interfaze}, handler));
    }

    @SuppressWarnings("unchecked")
    public <B, R extends CallableReference<B>> R cast(B target) throws IllegalArgumentException {
        InvocationHandler handler = Proxy.getInvocationHandler(target);
        if (handler instanceof JDKInvocationHandler) {
            JDKInvocationHandler<B> jdkHandler = (JDKInvocationHandler<B>) handler;
            return (R) jdkHandler.getServiceReference();
        } else if (handler instanceof JDKCallbackInvocationHandler) {
            // TODO return a CallbackReference
            throw new UnsupportedOperationException();
        } else {
            throw new IllegalArgumentException("Not a Fabric3 SCA proxy");
        }
    }
}
