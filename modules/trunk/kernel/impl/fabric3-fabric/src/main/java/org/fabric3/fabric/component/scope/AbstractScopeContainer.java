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

import org.fabric3.spi.AbstractLifecycle;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Implements functionality common to scope contexts.
 *
 * @version $Rev$ $Date$
 * @param <KEY> the type of identifier used to identify instances of this scope
 */
public abstract class AbstractScopeContainer<KEY> extends AbstractLifecycle implements ScopeContainer<KEY> {

    private static final Comparator<AtomicComponent<?>> COMPARATOR = new Comparator<AtomicComponent<?>>() {
        public int compare(AtomicComponent<?> o1, AtomicComponent<?> o2) {
            return o1.getInitLevel() - o2.getInitLevel();
        }
    };

    private final Scope<KEY> scope;
    private ScopeRegistry scopeRegistry;

    protected final ScopeContainerMonitor monitor;

    // the queue of components to eagerly initialize in each group
    protected final Map<URI, List<AtomicComponent<?>>> initQueues = new HashMap<URI, List<AtomicComponent<?>>>();

    // the queue of instanceWrappers to destroy, in the order that their instances were created
    protected final Map<KEY, List<InstanceWrapper<?>>> destroyQueues =
            new ConcurrentHashMap<KEY, List<InstanceWrapper<?>>>();

    public AbstractScopeContainer(Scope<KEY> scope, ScopeContainerMonitor monitor) {
        this.scope = scope;
        this.monitor = monitor;
    }

    public Scope<KEY> getScope() {
        return scope;
    }

    @Reference
    public void setScopeRegistry(ScopeRegistry scopeRegistry) {
        this.scopeRegistry = scopeRegistry;
    }

    @Init
    public synchronized void start() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != UNINITIALIZED && lifecycleState != STOPPED) {
            throw new IllegalStateException("Scope must be in UNINITIALIZED or STOPPED state [" + lifecycleState + "]");
        }
        if (scopeRegistry != null) {
            scopeRegistry.register(this);
        }
        setLifecycleState(RUNNING);
    }

    @Destroy
    public synchronized void stop() {
        int lifecycleState = getLifecycleState();
        if (lifecycleState != RUNNING) {
            throw new IllegalStateException("Scope in wrong state [" + lifecycleState + "]");
        }
        setLifecycleState(STOPPED);
        if (scopeRegistry != null) {
            scopeRegistry.unregister(this);
        }
        synchronized (initQueues) {
            initQueues.clear();
        }
        destroyQueues.clear();
    }

    public void register(AtomicComponent<?> component) {
        checkInit();
        if (component.isEagerInit()) {
            URI groupId = component.getGroupId();
            synchronized (initQueues) {
                List<AtomicComponent<?>> initQueue = initQueues.get(groupId);
                if (initQueue == null) {
                    initQueue = new ArrayList<AtomicComponent<?>>();
                    initQueues.put(groupId, initQueue);
                }
                // FIXME it would be more efficient to binary search and then insert
                initQueue.add(component);
                Collections.sort(initQueue, COMPARATOR);
            }
        }
    }

    public void unregister(AtomicComponent<?> component) {
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

    public void stopContext(WorkContext workContext) {
        KEY contextId = workContext.peekCallFrame().getCorrelationId(scope.getIdentifierType());
        shutdownComponents(destroyQueues.get(contextId));
        destroyQueues.remove(contextId);
    }

    public void initializeComponents(List<AtomicComponent<?>> components, URI groupId, WorkContext workContext)
            throws GroupInitializationException {
        List<Exception> causes = null;
        for (AtomicComponent<?> component : components) {
            try {
                getWrapper(component, workContext);
            } catch (Exception e) {
                monitor.eagerInitializationError(component.getUri(), e);
                if (causes == null) {
                    causes = new ArrayList<Exception>();
                }
                causes.add(e);
            }
        }
        if (causes != null) {
            throw new GroupInitializationException(groupId.toString(), causes);
        }
    }

    public String toString() {
        return "In state [" + super.toString() + ']';
    }

    protected void startContext(WorkContext workContext, KEY contextId, URI groupId) throws GroupInitializationException {
        destroyQueues.put(contextId, new ArrayList<InstanceWrapper<?>>());
        if (groupId != null) {
            // get and clone initialization queue
            List<AtomicComponent<?>> initQueue;
            synchronized (initQueues) {
                initQueue = initQueues.get(groupId);
                if (initQueue != null) {
                    initQueue = new ArrayList<AtomicComponent<?>>(initQueue);
                }
            }
            if (initQueue != null) {
                initializeComponents(initQueue, groupId, workContext);
            }
        }
    }

    /**
     * Shut down an ordered list of instances. The list passed to this method is treated as a live, mutable list so any instances added to this list
     * as shutdown is occuring will also be shut down.
     *
     * @param instances the list of instances to shutdown
     */
    protected void shutdownComponents(List<InstanceWrapper<?>> instances) {
        while (true) {
            InstanceWrapper<?> toDestroy;
            synchronized (instances) {
                if (instances.size() == 0) {
                    return;
                }
                toDestroy = instances.remove(instances.size() - 1);
            }
            try {
                toDestroy.stop();
            } catch (TargetDestructionException e) {
                // log the error from destroy but continue
                monitor.destructionError(e);
            }
        }
    }

    /**
     * Creates a new physical instance of a component, wrapped in an InstanceWrapper.
     *
     * @param component   the component whose instance should be created
     * @param workContext the work context in which to create the instance
     * @return a wrapped instance that has been injected but not yet started
     * @throws TargetResolutionException if there was a problem creating the instance
     */
    protected <T> InstanceWrapper<T> createInstance(AtomicComponent<T> component, WorkContext workContext)
            throws TargetResolutionException {
        try {
            return component.createInstanceWrapper(workContext);
        } catch (ObjectCreationException e) {
            throw new TargetResolutionException(e.getMessage(), component.getUri().toString(), e);
        }
    }

    private void checkInit() {
        if (getLifecycleState() != RUNNING) {
            throw new IllegalStateException("Scope container not running [" + getLifecycleState() + "]");
        }
    }

}
