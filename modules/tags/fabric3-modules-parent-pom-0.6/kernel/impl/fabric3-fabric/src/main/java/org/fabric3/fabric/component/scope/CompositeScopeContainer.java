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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;

/**
 * A scope context which manages atomic component instances keyed by composite
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ScopeContainer.class)
public class CompositeScopeContainer extends AbstractScopeContainer<URI> {
    // there is one instance per component so we can index directly
    private final Map<AtomicComponent<?>, InstanceWrapper<?>> instanceWrappers = new ConcurrentHashMap<AtomicComponent<?>, InstanceWrapper<?>>();

    // the queue of components to eagerly initialize in each group
    protected final Map<URI, List<AtomicComponent<?>>> initQueues = new HashMap<URI, List<AtomicComponent<?>>>();

    public CompositeScopeContainer(@Monitor ScopeContainerMonitor monitor) {
        super(Scope.COMPOSITE, monitor);
    }

    public void register(AtomicComponent<?> component) {
        super.register(component);
        if (component.isEagerInit()) {
            URI groupId = component.getGroupId();
            synchronized (initQueues) {
                List<AtomicComponent<?>> initQueue = initQueues.get(groupId);
                if (initQueue == null) {
                    initQueue = new ArrayList<AtomicComponent<?>>();
                    initQueues.put(groupId, initQueue);
                }
                initQueue.add(component);
                Collections.sort(initQueue, COMPARATOR);
            }
        }
        instanceWrappers.put(component, EMPTY);
    }

    public void unregister(AtomicComponent<?> component) {
        super.unregister(component);
        // FIXME should this component be destroyed already or do we need to stop it?
        instanceWrappers.remove(component);
        if (component.isEagerInit()) {
            URI groupId = component.getGroupId();
            synchronized (initQueues) {
                List<AtomicComponent<?>> initQueue = initQueues.get(groupId);
                initQueue.remove(component);
                if (initQueue.isEmpty()) {
                    initQueues.remove(groupId);
                }
            }
        }
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration policies
        startContext(workContext);
    }

    public void startContext(WorkContext workContext) throws GroupInitializationException {
        URI contextId = workContext.peekCallFrame().getCorrelationId(URI.class);
        super.startContext(workContext, contextId);
        eagerInitialize(workContext, contextId);

    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        URI contextId = workContext.peekCallFrame().getCorrelationId(URI.class);
        super.joinContext(workContext, contextId);
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration policies
        joinContext(workContext);
    }

    public void stopContext(WorkContext workContext) {
        URI contextId = workContext.peekCallFrame().getCorrelationId(URI.class);
        super.stopContext(workContext, contextId);
    }

    public synchronized void stop() {
        super.stop();
        synchronized (initQueues) {
            initQueues.clear();
        }
        instanceWrappers.clear();
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws InstanceLifecycleException {

        assert instanceWrappers.containsKey(component);
        @SuppressWarnings("unchecked")
        InstanceWrapper<T> wrapper = (InstanceWrapper<T>) instanceWrappers.get(component);
        if (wrapper != EMPTY) {
            return wrapper;
        }

        // FIXME is there a potential race condition here that may result in two instances being created
        try {
            wrapper = component.createInstanceWrapper(workContext);
        } catch (ObjectCreationException e) {
            throw new InstanceLifecycleException(e.getMessage(), component.getUri().toString(), e);
        }
        // some component instances such as system singletons may already be started
        if (!wrapper.isStarted()) {
            wrapper.start();
            List<InstanceWrapper<?>> queue = destroyQueues.get(component.getGroupId());
            if (queue == null) {
                throw new IllegalStateException("Context not started");
            }
            queue.add(wrapper);
        }
        instanceWrappers.put(component, wrapper);
        return wrapper;
    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws InstanceDestructionException {
    }

    public void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key) {
        InstanceWrapper<?> wrapper = instanceWrappers.get(component);
        if (wrapper != null) {
            wrapper.addObjectFactory(referenceName, factory, key);
        }
    }

    public void reinject() throws InstanceLifecycleException {
        for (InstanceWrapper<?> instanceWrapper : instanceWrappers.values()) {
            instanceWrapper.reinject();
        }
    }

    protected void stopContext(WorkContext workContext, URI contextId) {
        List<InstanceWrapper<?>> list = removeDestroyComponents(contextId);
        if (list == null) {
            throw new IllegalStateException("Context does not exist: " + contextId);
        }
        destroyInstances(list);
    }


    /**
     * Removes and returns components from the destroy queue under the given composite hierarchy. This method will recurse down the composite
     * hierarchy, including children in the list of components to shutdown.
     *
     * @param contextId the URI composite
     * @return the list of components to shutdown.
     */
    protected List<InstanceWrapper<?>> removeDestroyComponents(URI contextId) {
        // for composite contexts being closed, also destroy child composites and their contained components
        String path = contextId.getPath();
        List<InstanceWrapper<?>> toDestroy = new ArrayList<InstanceWrapper<?>>();
        for (Map.Entry<URI, List<InstanceWrapper<?>>> entry : destroyQueues.entrySet()) {
            URI key = entry.getKey();
            if (key.getPath().startsWith(path)) {
                // matches URIs that are in the hieratchy being destroyed
                List<InstanceWrapper<?>> wrappers = entry.getValue();
                toDestroy.addAll(wrappers);
                // safe to be removed during iteration
                destroyQueues.remove(key);
            }
        }
        return toDestroy;
    }

    private void eagerInitialize(WorkContext workContext, URI contextId) throws GroupInitializationException {
        // get and clone initialization queue
        List<AtomicComponent<?>> initQueue;
        synchronized (initQueues) {
            initQueue = initQueues.get(contextId);
            if (initQueue != null) {
                initQueue = new ArrayList<AtomicComponent<?>>(initQueue);
            }
        }
        if (initQueue != null) {
            initializeComponents(initQueue, workContext);
        }
    }

    private static final Comparator<AtomicComponent<?>> COMPARATOR = new Comparator<AtomicComponent<?>>() {
        public int compare(AtomicComponent<?> o1, AtomicComponent<?> o2) {
            return o1.getInitLevel() - o2.getInitLevel();
        }
    };

    private static final InstanceWrapper<Object> EMPTY = new InstanceWrapper<Object>() {
        public Object getInstance() {
            return null;
        }

        public boolean isStarted() {
            return true;
        }

        public void start() throws InstanceInitializationException {
        }

        public void stop() throws InstanceDestructionException {
        }

        public void reinject() {
        }

        public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {

        }

    };

}
