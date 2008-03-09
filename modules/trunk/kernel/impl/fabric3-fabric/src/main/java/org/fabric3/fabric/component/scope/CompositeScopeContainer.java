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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetInitializationException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.services.event.Fabric3Event;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.ExpirationPolicy;

/**
 * A scope context which manages atomic component instances keyed by composite
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ScopeContainer.class)
public class CompositeScopeContainer extends AbstractScopeContainer<URI> implements Fabric3EventListener{
    
    private static final InstanceWrapper<Object> EMPTY = new InstanceWrapper<Object>() {
        public Object getInstance() {
            return null;
        }

        public boolean isStarted() {
            return true;
        }

        public void start() throws TargetInitializationException {
        }

        public void stop() throws TargetDestructionException {
        }
        
        public void inject() {
        }
        
    };

    // there is one instance per component so we can index directly
    private final Map<AtomicComponent<?>, InstanceWrapper<?>> instanceWrappers = new ConcurrentHashMap<AtomicComponent<?>, InstanceWrapper<?>>();

    public CompositeScopeContainer(@Monitor ScopeContainerMonitor monitor) {
        super(Scope.COMPOSITE, monitor);
    }

    public void register(AtomicComponent<?> component) {
        super.register(component);
        instanceWrappers.put(component, EMPTY);
    }

    public void unregister(AtomicComponent<?> component) {
        // FIXME should this component be destroyed already or do we need to stop it?
        instanceWrappers.remove(component);
        super.unregister(component);
    }

    public void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException {
        super.startContext(workContext, groupId, groupId);
    }

    public void startContext(WorkContext workContext, URI groupId, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration
        this.startContext(workContext, groupId);
    }

    public void stopContext(WorkContext workContext) {
        URI contextId = workContext.peekCallFrame().getCorrelationId(URI.class);
        super.stopContext(contextId);
    }

    public synchronized void stop() {
        super.stop();
        instanceWrappers.clear();
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
        assert instanceWrappers.containsKey(component);
        @SuppressWarnings("unchecked")
        InstanceWrapper<T> wrapper = (InstanceWrapper<T>) instanceWrappers.get(component);
        if (wrapper != EMPTY) {
            return wrapper;
        }

        // FIXME is there a potential race condition here that may result in two instances being created
        wrapper = createInstance(component, workContext);
        wrapper.inject();
        if (!wrapper.isStarted()) {
            wrapper.start();
            destroyQueues.get(component.getGroupId()).add(wrapper);
        }
        instanceWrappers.put(component, wrapper);
        return wrapper;
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws TargetDestructionException {
    }

    public void onEvent(Fabric3Event event) {
        for (InstanceWrapper<?> instanceWrapper : instanceWrappers.values()) {
            instanceWrapper.inject();
        }
    }
}
