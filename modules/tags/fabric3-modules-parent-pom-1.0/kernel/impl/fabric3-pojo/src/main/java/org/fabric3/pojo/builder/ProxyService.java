/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.pojo.builder;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import org.oasisopen.sca.ServiceReference;

import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Creates proxies that implement Java interfaces and invocation handlers for fronting wires
 *
 * @version $Rev$ $Date$
 */

public interface ProxyService {
    /**
     * Create an ObjectFactory that provides proxies for the forward wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param type        the proxy type, i.e. stateless, conversational or propagates conversations
     * @param wire        the wire to proxy @return an ObjectFactory that will create proxies
     * @param callbackUri the callback URI or null if the wire is unidirectional
     * @return the factory
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<T> createObjectFactory(Class<T> interfaze, InteractionType type, Wire wire, String callbackUri) throws ProxyCreationException;

    /**
     * Create an ObjectFactory that provides proxies for the callback wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param container   the the scope container that manages component implementations where proxies created by the object factory will be injected
     * @param callbackUri the callback service uri
     * @param wire        the wire to proxy
     * @return an ObjectFactory that will create proxies
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<T> createCallbackObjectFactory(Class<T> interfaze, ScopeContainer container, URI callbackUri, Wire wire)
            throws ProxyCreationException;

    /**
     * Updates an ObjectFactory with an additional callback wire. This is used when multiple clients are wired to a target bidirectional service.
     *
     * @param factory     the ObjectFactory to update
     * @param interfaze   the interface the proxy implements
     * @param container   the the scope container that manages component implementations where proxies created by the object factory will be injected
     * @param callbackUri the callback service uri
     * @param wire        the wire to proxy
     * @return an ObjectFactory that will create proxies
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> ObjectFactory<?> updateCallbackObjectFactory(ObjectFactory<?> factory,
                                                     Class<T> interfaze,
                                                     ScopeContainer container,
                                                     URI callbackUri,
                                                     Wire wire) throws ProxyCreationException;


    /**
     * Creates a Java proxy for the given wire.
     *
     * @param interfaze   the interface the proxy implements
     * @param type        the interaction style for the wire
     * @param callbackUri the callback URI fr the wire fronted by the proxy or null if the wire is unidirectional
     * @param mappings    the method to invocation chain mappings
     * @return the proxy
     * @throws ProxyCreationException if there was a problem creating the proxy
     */
    <T> T createProxy(Class<T> interfaze, InteractionType type, String callbackUri, Map<Method, InvocationChain> mappings)
            throws ProxyCreationException;

    /**
     * Creates a Java proxy for the callback invocations chains.
     *
     * @param interfaze the interface the proxy should implement
     * @param mappings  the invocation chain mappings keyed by target URI @return the proxy
     * @return the proxy instance
     * @throws ProxyCreationException if an error is encountered during proxy generation
     */
    <T> T createCallbackProxy(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) throws ProxyCreationException;

    /**
     * Creates a callback proxy that allways returns to the same target service
     *
     * @param interfaze the service interface
     * @param mapping   the invocation chain mapping for the callback service
     * @param container the scope container that manages the implementation instance the proxy is injected on
     * @return the proxy instance
     */
    <T> T createStatefullCallbackProxy(Class<T> interfaze, Map<Method, InvocationChain> mapping, ScopeContainer<?> container);

    /**
     * Cast a proxy to a ServiceReference.
     *
     * @param target a proxy generated by this implementation
     * @return a ServiceReference equivalent to this proxy
     * @throws IllegalArgumentException if the object supplied is not a proxy
     */
    <B, R extends ServiceReference<B>> R cast(B target) throws IllegalArgumentException;

}
