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
package org.fabric3.pojo.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.CallFrame;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;
import org.fabric3.spi.wire.Message;
import org.fabric3.scdl.Scope;

/**
 * Responsible for dispatching an invocation to a Java component implementation instance.
 *
 * @version $Rev$ $Date$
 * @param <T> the implementation class for the component being invoked
 * @param <CONTEXT> the type of context id used by the ScopeContainer
 */
public class InvokerInterceptor<T, CONTEXT> implements Interceptor {
    private Method operation;
    private AtomicComponent<T> component;
    private ScopeContainer<CONTEXT> scopeContainer;
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
                              ScopeContainer<CONTEXT> scopeContainer) {
        this.operation = operation;
        this.callback = callback;
        this.endConversation = endConversation;
        this.component = component;
        this.scopeContainer = scopeContainer;
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
        Object body = msg.getBody();
        WorkContext workContext = msg.getWorkContext();
        InstanceWrapper<T> wrapper;
        try {
            CallFrame frame = workContext.peekCallFrame();
            // for now tolerate callframes not being set as bindings may not be adding them for incoming service invocations
            if (frame != null) {
                if (!callback) {
                    // check if this is a callback. If so, do not start the conversation since it has already been done by the forward invocation
                    if (conversationScope && frame.isStartConversation()) {
                        // start the conversation context
                        if (component.getMaxAge() > 0) {
                            ExpirationPolicy policy = new NonRenewableExpirationPolicy(System.currentTimeMillis() + component.getMaxAge());
                            scopeContainer.startContext(workContext, null, policy);
                        } else if (component.getMaxIdleTime() > 0) {
                            long expire = System.currentTimeMillis() + component.getMaxIdleTime();
                            ExpirationPolicy policy = new RenewableExpirationPolicy(expire, component.getMaxIdleTime());
                            scopeContainer.startContext(workContext, null, policy);
                        } else {
                            scopeContainer.startContext(workContext, null);
                        }
                    }
                }
            }
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (TargetResolutionException e) {
            throw new InvocationRuntimeException(e);
        } catch (GroupInitializationException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            WorkContext oldWorkContext = PojoWorkContextTunnel.setThreadWorkContext(workContext);
            try {
                msg.setBody(operation.invoke(instance, (Object[]) body));
            } catch (InvocationTargetException e) {
                msg.setBodyWithFault(e.getCause());
            } catch (IllegalAccessException e) {
                throw new InvocationRuntimeException(e);
            } finally {
                PojoWorkContextTunnel.setThreadWorkContext(oldWorkContext);
            }
            return msg;
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
                if (conversationScope && endConversation) {
                    scopeContainer.stopContext(workContext);
                }
            } catch (TargetDestructionException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }
}
