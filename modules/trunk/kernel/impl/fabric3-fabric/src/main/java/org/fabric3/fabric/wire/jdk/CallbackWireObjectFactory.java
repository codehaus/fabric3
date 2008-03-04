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

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;

/**
 * Returns a proxy instance for a callback wire.
 *
 * @version $Rev$ $Date$
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private Scope sourceScope;
    private ProxyService proxyService;
    private Map<String, Map<Method, InvocationChain>> mappings;
    private Class<T> interfaze;

    /**
     * Constructor.
     *
     * @param interfaze    the proxy interface
     * @param sourceScope  the scope of the component implementation the proxy will be injected on
     * @param proxyService the service for creating proxies
     * @param mappings     the callback URI to invocation chain mappings
     */
    public CallbackWireObjectFactory(Class<T> interfaze,
                                     Scope sourceScope,
                                     ProxyService proxyService,
                                     Map<String, Map<Method, InvocationChain>> mappings) {
        this.interfaze = interfaze;
        this.sourceScope = sourceScope;
        this.proxyService = proxyService;
        this.mappings = mappings;
    }

    public T getInstance() throws ObjectCreationException {
        if (Scope.COMPOSITE.equals(sourceScope)) {
            return interfaze.cast(proxyService.createCallbackProxy(interfaze, mappings));
        } else {
            CallFrame frame = PojoWorkContextTunnel.getThreadWorkContext().peekCallFrame();
            String callbackUri = frame.getCallbackUri();
            assert callbackUri != null;
            Map<Method, InvocationChain> mapping = mappings.get(callbackUri);
            assert mapping != null;
            return interfaze.cast(proxyService.createStatefullCallbackProxy(interfaze, mapping));
        }
    }

}
