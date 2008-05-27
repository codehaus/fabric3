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
package org.fabric3.fabric.component.scope;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.scdl.Scope;

/**
 * The default implementation of a scope registry
 *
 * @version $Rev$ $Date$
 */
public class ScopeRegistryImpl implements ScopeRegistry {
    private final Map<String, ScopeContainer<?>> scopes = new ConcurrentHashMap<String, ScopeContainer<?>>();

    public synchronized <T> void register(ScopeContainer<T> container) {
        Scope scope = container.getScope();
        scopes.put(scope.getScope(), container);
    }

    public synchronized <T> void unregister(ScopeContainer<T> container) {
        scopes.remove(container.getScope().getScope());
    }

    @SuppressWarnings("unchecked")
    public <T> ScopeContainer<T> getScopeContainer(Scope<T> scope) {
        return (ScopeContainer<T>) scopes.get(scope.getScope());
    }


    public Scope<?> getScope(String scopeName) {
        ScopeContainer<?> container = scopes.get(scopeName);
        return container == null ? null : container.getScope();
    }
}
