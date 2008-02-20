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
package org.fabric3.fabric.wire;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.ProxyService;
import org.fabric3.spi.wire.Wire;

/**
 * Uses a wire to return an object instance
 *
 * @version $Rev$ $Date$
 */
public class WireObjectFactory<T> implements ObjectFactory<T> {
    private Class<T> interfaze;
    private boolean conversational;
    private String callbackUri;
    private ProxyService proxyService;
    // the cache of proxy interface method to operation mappings
    private Map<Method, InvocationChain> mappings;

    /**
     * Constructor for proxies that front bi-directional services.
     *
     * @param interfaze      the interface to inject on the client
     * @param conversational if the wire is conversational
     * @param callbackUri    the callback URI for the wire or null if the wire is unidirectional
     * @param proxyService   the wire service to create the proxy
     * @param mappings       proxy method to invocation chain mappings
     * @throws NoMethodForOperationException if a method matching the operation cannot be found
     */
    public WireObjectFactory(Class<T> interfaze,
                             boolean conversational,
                             String callbackUri,
                             ProxyService proxyService,
                             Map<Method, InvocationChain> mappings) throws NoMethodForOperationException {
        this.interfaze = interfaze;
        this.conversational = conversational;
        this.callbackUri = callbackUri;
        this.proxyService = proxyService;
        this.mappings = mappings;
    }


    public T getInstance() throws ObjectCreationException {
        return interfaze.cast(proxyService.createProxy(interfaze, conversational, callbackUri, mappings));
    }
}

