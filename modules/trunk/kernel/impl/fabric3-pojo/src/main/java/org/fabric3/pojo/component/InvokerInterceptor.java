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
package org.fabric3.pojo.component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osoa.sca.ConversationEndedException;

import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.ConversationContext;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * Responsible for dispatching an invocation to a Java-based component implementation instance.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the component being invoked
 */
public class InvokerInterceptor<T> implements Interceptor {
    private Method operation;
    private AtomicComponent<T> component;
    private ScopeContainer scopeContainer;
    private ClassLoader targetTCCLClassLoader;
    private boolean callback;
    private boolean endConversation;
    private boolean conversationScope;

    /**
     * Creates a new interceptor instance.
     *
     * @param operation       the method to invoke on the target instance
     * @param callback        true if the operation is a callback
     * @param endConversation if true, ends the conversation after the invocation
     * @param component       the target component
     * @param scopeContainer  the ScopeContainer that manages implementation instances for the target component
     */
    public InvokerInterceptor(Method operation,
                              boolean callback,
                              boolean endConversation,
                              AtomicComponent<T> component,
                              ScopeContainer scopeContainer) {
        this.operation = operation;
        this.callback = callback;
        this.endConversation = endConversation;
        this.component = component;
        this.scopeContainer = scopeContainer;
        conversationScope = Scope.CONVERSATION.equals(scopeContainer.getScope());
    }

    /**
     * Creates a new interceptor instance that sets the TCCL to the given classloader before dispatching an invocation.
     *
     * @param operation             the method to invoke on the target instance
     * @param callback              true if the operation is a callback
     * @param endConversation       if true, ends the conversation after the invocation
     * @param component             the target component
     * @param scopeContainer        the ScopeContainer that manages implementation instances for the target component
     * @param targetTCCLClassLoader the classloader to set the TCCL to before dispatching.
     */
    public InvokerInterceptor(Method operation,
                              boolean callback,
                              boolean endConversation,
                              AtomicComponent<T> component,
                              ScopeContainer scopeContainer,
                              ClassLoader targetTCCLClassLoader) {
        this.operation = operation;
        this.callback = callback;
        this.endConversation = endConversation;
        this.component = component;
        this.scopeContainer = scopeContainer;
        this.targetTCCLClassLoader = targetTCCLClassLoader;
        conversationScope = Scope.CONVERSATION.equals(scopeContainer.getScope());
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last one in an target interceptor chain");
    }

    public Interceptor getNext() {
        return null;
    }

    public boolean isOptimizable() {
        return true;
    }

    public Message invoke(Message msg) {
        WorkContext workContext = msg.getWorkContext();
        InstanceWrapper<T> wrapper;
        try {
            startOrJoinContext(workContext);
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (ConversationEndedException e) {
            msg.setBodyWithFault(e);
            return msg;
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            return invoke(msg, workContext, instance);
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
                if (conversationScope && endConversation) {
                    scopeContainer.stopContext(workContext);
                }
            } catch (InstanceDestructionException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }

    /**
     * Performs the invocation on the target component instance. If a target classloader is configured for the interceptor, it will be set as the
     * TCCL.
     *
     * @param msg         the messaging containing the invocation data
     * @param workContext the current work context
     * @param instance    the target component instance
     * @return the response message
     */
    private Message invoke(Message msg, WorkContext workContext, Object instance) {
        WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            Object body = msg.getBody();
            if (targetTCCLClassLoader == null) {
                msg.setBody(operation.invoke(instance, (Object[]) body));
            } else {
                ClassLoader old = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(targetTCCLClassLoader);
                    msg.setBody(operation.invoke(instance, (Object[]) body));
                } finally {
                    Thread.currentThread().setContextClassLoader(old);
                }
            }
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e.getCause());
        } catch (IllegalAccessException e) {
            throw new InvocationRuntimeException(e);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldWorkContext);
        }
        return msg;
    }

    /**
     * Starts or joins a scope context.
     *
     * @param workContext the current work context
     * @throws InvocationRuntimeException if an error occurs starting or joining the context
     */
    private void startOrJoinContext(WorkContext workContext) throws InvocationRuntimeException {
        // Check if this is a callback. If so, do not start or join the conversation since it has already been done by the forward invocation
        // Also, if the target is not conversation scoped, no context needs to be started
        if (callback || !conversationScope) {
            return;
        }
        CallFrame frame = workContext.peekCallFrame();
        if (frame == null) {
            // For now tolerate callframes not being set as bindings may not be adding them for incoming service invocations
            return;
        }
        try {
            if (ConversationContext.NEW == frame.getConversationContext()) {
                // start the conversation context
                if (component.getMaxAge() > 0) {
                    ExpirationPolicy policy = new NonRenewableExpirationPolicy(System.currentTimeMillis() + component.getMaxAge());
                    scopeContainer.startContext(workContext, policy);
                } else if (component.getMaxIdleTime() > 0) {
                    long expire = System.currentTimeMillis() + component.getMaxIdleTime();
                    ExpirationPolicy policy = new RenewableExpirationPolicy(expire, component.getMaxIdleTime());
                    scopeContainer.startContext(workContext, policy);
                } else {
                    scopeContainer.startContext(workContext);
                }
            } else if (ConversationContext.PROPAGATE == frame.getConversationContext()) {
                if (component.getMaxAge() > 0) {
                    ExpirationPolicy policy = new NonRenewableExpirationPolicy(System.currentTimeMillis() + component.getMaxAge());
                    scopeContainer.joinContext(workContext, policy);
                } else if (component.getMaxIdleTime() > 0) {
                    long expire = System.currentTimeMillis() + component.getMaxIdleTime();
                    ExpirationPolicy policy = new RenewableExpirationPolicy(expire, component.getMaxIdleTime());
                    scopeContainer.joinContext(workContext, policy);
                } else {
                    scopeContainer.joinContext(workContext);
                }
            }
        } catch (GroupInitializationException e) {
            throw new InvocationRuntimeException(e);
        }
    }
}
