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
package org.fabric3.fabric.component.scope;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.model.type.component.Scope;
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
 * Scope container for the standard COMPOSITE scope. Composite components have one instance which is associated with a scope context. Components
 * deployed via a deployable composite are associated with the same context. When a context starts and stops, components will recieve initalization
 * and destruction callbacks.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ScopeContainer.class)
public class CompositeScopeContainer extends AbstractScopeContainer<QName> {
    private final Map<AtomicComponent<?>, InstanceWrapper<?>> instanceWrappers;

    // The map of InstanceWrappers to destroy keyed by the deployable composite the component was deployed with.
    // The queues of InstanceWrappers are ordered by the sequence in which the deployables were deployed.
    // The InstanceWrappers themselves are ordered by the sequence in which they were instantiated.
    private final Map<QName, List<InstanceWrapper<?>>> destroyQueues;

    // the queue of components to eagerly initialize in each group
    private final Map<QName, List<AtomicComponent<?>>> initQueues = new HashMap<QName, List<AtomicComponent<?>>>();

    public CompositeScopeContainer(@Monitor ScopeContainerMonitor monitor) {
        super(Scope.COMPOSITE, monitor);
        instanceWrappers = new ConcurrentHashMap<AtomicComponent<?>, InstanceWrapper<?>>();
        destroyQueues = new LinkedHashMap<QName, List<InstanceWrapper<?>>>();
    }

    public void register(AtomicComponent<?> component) {
        super.register(component);
        if (component.isEagerInit()) {
            QName deployable = component.getDeployable();
            synchronized (initQueues) {
                List<AtomicComponent<?>> initQueue = initQueues.get(deployable);
                if (initQueue == null) {
                    initQueue = new ArrayList<AtomicComponent<?>>();
                    initQueues.put(deployable, initQueue);
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
            QName deployable = component.getDeployable();
            synchronized (initQueues) {
                List<AtomicComponent<?>> initQueue = initQueues.get(deployable);
                initQueue.remove(component);
                if (initQueue.isEmpty()) {
                    initQueues.remove(deployable);
                }
            }
        }
    }

    public void startContext(WorkContext workContext) throws GroupInitializationException {
        QName contextId = workContext.peekCallFrame().getCorrelationId(QName.class);
        synchronized (destroyQueues) {
            destroyQueues.put(contextId, new ArrayList<InstanceWrapper<?>>());
        }
        eagerInitialize(workContext, contextId);
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration policies
        startContext(workContext);
    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        QName contextId = workContext.peekCallFrame().getCorrelationId(QName.class);
        synchronized (destroyQueues) {
            if (!destroyQueues.containsKey(contextId)) {
                destroyQueues.put(contextId, new ArrayList<InstanceWrapper<?>>());
            }
        }
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        // scope does not support expiration policies
        joinContext(workContext);
    }

    public void stopContext(WorkContext workContext) {
        QName contextId = workContext.peekCallFrame().getCorrelationId(QName.class);
        synchronized (destroyQueues) {
            List<InstanceWrapper<?>> list = destroyQueues.get(contextId);
            if (list == null) {
                throw new IllegalStateException("Context does not exist: " + contextId);
            }
            destroyInstances(list, workContext);
        }
    }

    public void stopAllContexts(WorkContext workContext) {
        synchronized (destroyQueues) {
            for (List<InstanceWrapper<?>> queue : destroyQueues.values()) {
                destroyInstances(queue, workContext);
            }
        }
    }

    public synchronized void stop() {
        super.stop();
        synchronized (destroyQueues) {
            destroyQueues.clear();
        }
        synchronized (initQueues) {
            initQueues.clear();
        }
        instanceWrappers.clear();
    }

    @SuppressWarnings({"unchecked"})
    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws InstanceLifecycleException {
        InstanceWrapper<T> wrapper = (InstanceWrapper<T>) instanceWrappers.get(component);
        if (wrapper != EMPTY) {
            return wrapper;
        }

        // FIXME is there a potential race condition here that may result in two instances being created
        try {
            wrapper = component.createInstanceWrapper(workContext);
        } catch (ObjectCreationException e) {
            String uri = component.getUri().toString();
            throw new InstanceLifecycleException("Error initializing: " + uri, uri, e);
        }
        // some component instances such as system singletons may already be started
        if (!wrapper.isStarted()) {
            wrapper.start(workContext);
            List<InstanceWrapper<?>> queue;
            QName deployable = component.getDeployable();
            synchronized (destroyQueues) {
                queue = destroyQueues.get(deployable);
            }
            if (queue == null) {
                throw new IllegalStateException("Context not started: " + deployable);
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


    private void eagerInitialize(WorkContext workContext, QName contextId) throws GroupInitializationException {
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

        public void start(WorkContext workContext) throws InstanceInitializationException {
        }

        public void stop(WorkContext workContext) throws InstanceDestructionException {
        }

        public void reinject() {
        }

        public void addObjectFactory(String referenceName, ObjectFactory<?> factory, Object key) {

        }

    };

}
