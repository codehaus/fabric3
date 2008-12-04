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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.osoa.sca.Conversation;
import org.osoa.sca.ConversationEndedException;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.WorkContext;

/**
 * Scope container for the standard CONVERSATIONAL scope.
 *
 * @version $Rev$ $Date$
 */
@Service(ScopeContainer.class)
@EagerInit
public class ConversationalScopeContainer extends AbstractScopeContainer<Conversation> {
    private final Map<Conversation, ExpirationPolicy> expirationPolicies;
    private final Map<Conversation, List<ConversationExpirationCallback>> expirationCallbacks;
    private final InstanceWrapperStore<Conversation> store;
    private ScheduledExecutorService executor;
    // TODO this should be part of the system configuration
    private long delay = 600;  // reap every 600 seconds

    // the queue of instanceWrappers to destroy, in the order that their instances were created
    private final Map<Conversation, List<InstanceWrapper<?>>> destroyQueues = new ConcurrentHashMap<Conversation, List<InstanceWrapper<?>>>();


    public ConversationalScopeContainer(@Monitor ScopeContainerMonitor monitor, @Reference(name = "store") InstanceWrapperStore<Conversation> store) {
        super(Scope.CONVERSATION, monitor);
        this.store = store;
        expirationPolicies = new ConcurrentHashMap<Conversation, ExpirationPolicy>();
        expirationCallbacks = new ConcurrentHashMap<Conversation, List<ConversationExpirationCallback>>();
    }

    /**
     * Optional property to set the delay for executing the reaper to clear expired conversation contexts
     *
     * @param delay the delay in seconds
     */
    @Property
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Init
    public void start() {
        super.start();
        executor = Executors.newSingleThreadScheduledExecutor();
        Runnable reaper = new Reaper();
        executor.scheduleWithFixedDelay(reaper, delay, delay, TimeUnit.SECONDS);
    }

    @Destroy
    public void stop() {
        executor.shutdownNow();
        destroyQueues.clear();
        super.stop();
    }

    public void registerCallback(Conversation conversation, ConversationExpirationCallback callback) {
        List<ConversationExpirationCallback> callbacks = expirationCallbacks.get(conversation);
        if (callbacks == null) {
            callbacks = new ArrayList<ConversationExpirationCallback>();
            expirationCallbacks.put(conversation, callbacks);
        }
        synchronized (callbacks) {
            callbacks.add(callback);
        }
    }

    public void startContext(WorkContext workContext) throws GroupInitializationException {
        startContext(workContext, null);
    }

    public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        store.startContext(conversation);
        destroyQueues.put(conversation, new ArrayList<InstanceWrapper<?>>());
        if (policy != null) {
            expirationPolicies.put(conversation, policy);
        }
    }

    public void joinContext(WorkContext workContext) throws GroupInitializationException {
        joinContext(workContext, null);
    }

    public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        if (!destroyQueues.containsKey(conversation)) {
            destroyQueues.put(conversation, new ArrayList<InstanceWrapper<?>>());
            if (policy != null) {
                expirationPolicies.put(conversation, policy);
            }
        }
    }

    public void stopContext(WorkContext workContext) {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        stopContext(conversation);
        expirationPolicies.remove(conversation);
        notifyExpirationCallbacks(conversation);
    }

    private void stopContext(Conversation conversation) {
        List<InstanceWrapper<?>> list = destroyQueues.remove(conversation);
        if (list == null) {
            throw new IllegalStateException("Conversation does not exist: " + conversation);
        }
        destroyInstances(list);

        store.stopContext(conversation);
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws InstanceLifecycleException {
        CallFrame frame = workContext.peekCallFrame();
        Conversation conversation = frame.getConversation();
        assert conversation != null;
        ExpirationPolicy policy = expirationPolicies.get(conversation);
        if (policy != null && !policy.isExpired()) {
            // renew the conversation expiration if one is associated, i.e. it is an expiring conversation
            expirationPolicies.get(conversation).renew();
        }
        ConversationContext context = frame.getConversationContext();
        // if the context is new or propagates a conversation and the target instance has not been created, create it
        boolean create = (context == ConversationContext.NEW || context == ConversationContext.PROPAGATE);
        InstanceWrapper<T> wrapper = getWrapper(component, workContext, conversation, create);
        if (wrapper == null) {
            // conversation has either been ended or timed out, throw an exception
            throw new ConversationEndedException("Conversation ended");
        }
        return wrapper;
    }

    public void reinject() {
    }

    public void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key) {

    }

    public <T> void returnWrapper(AtomicComponent<T> component, WorkContext workContext, InstanceWrapper<T> wrapper)
            throws InstanceDestructionException {
    }


    private void notifyExpirationCallbacks(Conversation conversation) {
        List<ConversationExpirationCallback> callbacks = expirationCallbacks.remove(conversation);
        if (callbacks != null) {
            synchronized (callbacks) {
                for (ConversationExpirationCallback callback : callbacks) {
                    callback.expire(conversation);
                }
            }
        }
    }


    /**
     * Periodically scans and removes expired conversation contexts.
     */
    private class Reaper implements Runnable {
        public void run() {
            for (Iterator<Map.Entry<Conversation, ExpirationPolicy>> iterator = expirationPolicies.entrySet().iterator(); iterator.hasNext();) {
                Map.Entry<Conversation, ExpirationPolicy> entry = iterator.next();
                if (entry.getValue().isExpired()) {
                    Conversation conversation = entry.getKey();
                    iterator.remove();
                    WorkContext workContext = new WorkContext();
                    CallFrame frame = new CallFrame(null, conversation, conversation, null);
                    workContext.addCallFrame(frame);
                    stopContext(conversation);
                    notifyExpirationCallbacks(conversation);
                }
            }
        }
    }

    /**
     * Return an instance wrapper containing a component implementation instance associated with the correlation key, optionally creating one if not
     * found.
     *
     * @param component    the component the implementation instance belongs to
     * @param workContext  the current WorkContext
     * @param conversation the conversation key for the component implementation instance
     * @param create       true if an instance should be created
     * @return an instance wrapper or null if not found an create is set to false
     * @throws org.fabric3.spi.component.InstanceLifecycleException
     *          if an error occurs returning the wrapper
     */
    private <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext, Conversation conversation, boolean create)
            throws InstanceLifecycleException {
        assert conversation != null;
        InstanceWrapper<T> wrapper = store.getWrapper(component, conversation);
        if (wrapper == null && create) {
            try {
                wrapper = component.createInstanceWrapper(workContext);
            } catch (ObjectCreationException e) {
                throw new InstanceLifecycleException(e.getMessage(), component.getUri().toString(), e);
            }
            wrapper.start(workContext);
            store.putWrapper(component, conversation, wrapper);
            List<InstanceWrapper<?>> queue = destroyQueues.get(conversation);
            if (queue == null) {
                throw new IllegalStateException("Instance context not found");
            }
            queue.add(wrapper);
        }
        return wrapper;
    }


}
