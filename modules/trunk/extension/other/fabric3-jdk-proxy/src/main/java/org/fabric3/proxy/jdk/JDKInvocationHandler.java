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
package org.fabric3.proxy.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;

import org.osoa.sca.Conversation;
import org.osoa.sca.ServiceReference;
import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.pojo.ConversationImpl;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.component.InstanceInvocationException;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ConversationExpirationCallback;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.InteractionType;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.ConversationContext;

/**
 * Dispatches from a proxy to a wire.
 *
 * @version $Rev: 3021 $ $Date: 2008-03-03 19:28:04 -0800 (Mon, 03 Mar 2008) $
 */
public final class JDKInvocationHandler<B> implements ConversationExpirationCallback, InvocationHandler, ServiceReference<B> {
    private final Class<B> businessInterface;
    private final B proxy;
    private final InteractionType type;
    private final Map<Method, InvocationChain> chains;
    private final ScopeContainer<Conversation> scopeContainer;

    private Conversation conversation;
    private Object userConversationId;
    private String callbackUri;

    /**
     * Creates a stateless wire proxy
     *
     * @param interfaze   the proxy interface
     * @param callbackUri the callback uri or null if the wire is unidirectional
     * @param mapping     the method to invocation chain mappings for the wire
     * @throws NoMethodForOperationException if an error occurs creating the proxy
     */
    public JDKInvocationHandler(Class<B> interfaze, String callbackUri, Map<Method, InvocationChain> mapping)
            throws NoMethodForOperationException {
        this(interfaze, InteractionType.STATELESS, callbackUri, mapping, null);
    }

    /**
     * Creates a wire proxy.
     *
     * @param interfaze      the proxy interface
     * @param type           the interaction style for the wire
     * @param callbackUri    the callback uri or null if the wire is unidirectional
     * @param mapping        the method to invocation chain mappings for the wire
     * @param scopeContainer the conversational scope container
     * @throws NoMethodForOperationException if an error occurs creating the proxy
     */
    public JDKInvocationHandler(Class<B> interfaze,
                                InteractionType type,
                                String callbackUri,
                                Map<Method, InvocationChain> mapping,
                                ScopeContainer<Conversation> scopeContainer) throws NoMethodForOperationException {
        this.callbackUri = callbackUri;
        assert mapping != null;
        this.businessInterface = interfaze;
        ClassLoader loader = interfaze.getClassLoader();
        this.proxy = interfaze.cast(Proxy.newProxyInstance(loader, new Class[]{interfaze}, this));
        this.chains = mapping;
        this.scopeContainer = scopeContainer;
        this.type = type;
    }


    public void expire(Conversation conversation) {
        this.conversation = null;
    }

    public B getService() {
        return proxy;
    }

    public ServiceReference<B> getServiceReference() {
        return this;
    }

    public boolean isConversational() {
        return type != InteractionType.STATELESS;
    }

    public Class<B> getBusinessInterface() {
        return businessInterface;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public Object getConversationID() {
        return userConversationId;
    }

    public void setConversationID(Object conversationId) throws IllegalStateException {
        if (conversation != null) {
            throw new IllegalStateException("A conversation is already active");
        }
        userConversationId = conversationId;
    }

    public Object getCallbackID() {
        throw new UnsupportedOperationException();
    }

    public void setCallbackID(Object callbackID) {
        throw new UnsupportedOperationException();
    }

    public Object getCallback() {
        throw new UnsupportedOperationException();
    }

    public void setCallback(Object callback) {
        throw new UnsupportedOperationException();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }

        Interceptor headInterceptor = chain.getHeadInterceptor();
        assert headInterceptor != null;

        WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
        CallFrame frame;
        if (InteractionType.CONVERSATIONAL == type && conversation == null) {
            conversation = new ConversationImpl(createConversationID(), scopeContainer);
            // register this proxy to receive notifications when the conversation ends
            scopeContainer.registerCallback(conversation, this);
            // mark the CallFrame as starting a conversation
            frame = new CallFrame(callbackUri, null, conversation, ConversationContext.NEW);
        } else if (InteractionType.PROPAGATES_CONVERSATION == type && conversation == null) {
            Conversation propagated = workContext.peekCallFrame().getConversation();
            frame = new CallFrame(callbackUri, null, propagated, ConversationContext.PROPAGATE);
        } else {
            frame = new CallFrame(callbackUri, null, conversation, null);
        }
        workContext.addCallFrame(frame);
        Message msg = new MessageImpl();
        msg.setBody(args);
        msg.setWorkContext(workContext);
        try {
            // dispatch the invocation down the chain and get the response
            Message resp;
            try {
                resp = headInterceptor.invoke(msg);
            } catch (ServiceUnavailableException e) {
                // simply rethrow ServiceUnavailableExceptions
                throw e;
            } catch (RuntimeException e) {
                // wrap other exceptions raised by the runtime
                throw new ServiceUnavailableException(e);
            }

            // handle response from the application, returning or throwing an exception as appropriate
            Object body = resp.getBody();
            if (resp.isFault()) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            if (InteractionType.CONVERSATIONAL == type || InteractionType.PROPAGATES_CONVERSATION == type) {
                PhysicalOperationDefinition operation = chain.getPhysicalOperation();
                if (operation.isEndsConversation()) {
                    conversation = null;
                }
            }
            workContext.popCallFrame();
        }

    }

    /**
     * Creates a new conversational id
     *
     * @return the conversational id
     */
    private Object createConversationID() {
        if (userConversationId != null) {
            return userConversationId;
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private Object handleProxyMethod(Method method) throws InstanceInvocationException {
        if (method.getParameterTypes().length == 0 && "toString".equals(method.getName())) {
            return "[Proxy - " + Integer.toHexString(hashCode()) + "]";
        } else if (method.getDeclaringClass().equals(Object.class)
                && "equals".equals(method.getName())) {
            // TODO implement
            throw new UnsupportedOperationException();
        } else if (Object.class.equals(method.getDeclaringClass())
                && "hashCode".equals(method.getName())) {
            return hashCode();
            // TODO beter hash algorithm
        }
        String op = method.getName();
        throw new InstanceInvocationException("Operation not configured: " + op, op);
    }
}
