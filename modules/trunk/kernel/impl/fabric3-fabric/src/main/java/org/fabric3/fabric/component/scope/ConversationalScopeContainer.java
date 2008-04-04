/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
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
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;

/**
 * Scope container for the standard CONVERSATIONAL scope.
 *
 * @version $Rev$ $Date$
 */
@Service(ScopeContainer.class)
@EagerInit
public class ConversationalScopeContainer extends StatefulScopeContainer<Conversation> {
    private final ConcurrentHashMap<Conversation, ExpirationPolicy> expirationPolicies;
    private final ConcurrentHashMap<Conversation, List<ConversationExpirationCallback>> expirationCallbacks;
    private ScheduledExecutorService executor;
    // TODO this should be part of the system configuration
    private long delay = 600;  // reap every 600 seconds

    public ConversationalScopeContainer(@Monitor ScopeContainerMonitor monitor,
                                        @Reference(name = "store")InstanceWrapperStore<Conversation> store) {
        super(Scope.CONVERSATION, monitor, store);
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

    public void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        super.startContext(workContext, conversation, groupId);
    }

    public void startContext(WorkContext workContext, URI groupId, ExpirationPolicy policy) throws GroupInitializationException {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        super.startContext(workContext, conversation, groupId);
        expirationPolicies.put(conversation, policy);
    }

    public void stopContext(WorkContext workContext) {
        Conversation conversation = workContext.peekCallFrame().getConversation();
        assert conversation != null;
        super.stopContext(conversation);
        expirationPolicies.remove(conversation);
        notifyExpirationCallbacks(conversation);
    }

    public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
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
        InstanceWrapper<T> wrapper = super.getWrapper(component, workContext, conversation, create);
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
                    stopContext(conversation);
                    notifyExpirationCallbacks(conversation);
                }
            }
        }
    }

}
