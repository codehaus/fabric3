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
package org.fabric3.fabric.component.scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.StoreException;

/**
 * A simple store that just retains instances in memory without expiration. Basically, a HashMap.
 *
 * @version $Rev$ $Date$
 */
public class NonExpiringMemoryStore<KEY> implements InstanceWrapperStore<KEY> {
    private final Map<KEY, Map<AtomicComponent<?>, InstanceWrapper<?>>> contexts =
            new ConcurrentHashMap<KEY, Map<AtomicComponent<?>, InstanceWrapper<?>>>();

    public void startContext(KEY contextId) throws StoreException {
        contexts.put(contextId, new ConcurrentHashMap<AtomicComponent<?>, InstanceWrapper<?>>());
    }

    public void stopContext(KEY contextId) throws StoreException {
        contexts.remove(contextId);
    }

    @SuppressWarnings("unchecked")
    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, KEY contextId) throws StoreException {
        Map<AtomicComponent<?>, InstanceWrapper<?>> context = contexts.get(contextId);
        if (context == null) {
            return null;
        }
        return (InstanceWrapper<T>) context.get(component);
    }

    public <T> void putWrapper(AtomicComponent<T> component, KEY contextId, InstanceWrapper<T> wrapper)
            throws StoreException {
        Map<AtomicComponent<?>, InstanceWrapper<?>> context = contexts.get(contextId);
        assert context != null;
        context.put(component, wrapper);
    }
}
