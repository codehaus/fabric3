/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.system.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationRuntimeException;

/**
 * @version $Rev$ $Date$
 */
public class SystemInvokerInterceptor<T> implements Interceptor {

    private final Method operation;
    private final ScopeContainer<?> scopeContainer;
    private final AtomicComponent<T> component;

    public SystemInvokerInterceptor(Method operation, ScopeContainer<?> scopeContainer, AtomicComponent<T> component) {
        this.operation = operation;
        this.scopeContainer = scopeContainer;
        this.component = component;
    }

    public void setNext(Interceptor next) {
        throw new UnsupportedOperationException();
    }

    public Interceptor getNext() {
        return null;
    }

    public Message invoke(Message msg) {
        Object body = msg.getBody();
        WorkContext workContext = msg.getWorkContext();
        InstanceWrapper<T> wrapper;
        try {
            wrapper = scopeContainer.getWrapper(component, workContext);
        } catch (InstanceLifecycleException e) {
            throw new InvocationRuntimeException(e);
        }

        try {
            Object instance = wrapper.getInstance();
            WorkContext oldWorkContext = WorkContextTunnel.setThreadWorkContext(workContext);
            try {
                msg.setBody(operation.invoke(instance, (Object[]) body));
            } catch (InvocationTargetException e) {
                msg.setBodyWithFault(e.getCause());
            } catch (IllegalAccessException e) {
                throw new InvocationRuntimeException(e);
            } finally {
                WorkContextTunnel.setThreadWorkContext(oldWorkContext);
            }
            return msg;
        } finally {
            try {
                scopeContainer.returnWrapper(component, workContext, wrapper);
            } catch (InstanceDestructionException e) {
                throw new InvocationRuntimeException(e);
            }
        }
    }
}
