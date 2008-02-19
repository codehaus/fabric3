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
package org.fabric3.fabric.wire.jdk;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.component.TargetInvocationException;
import org.fabric3.pojo.PojoWorkContextTunnel;

/**
 * Responsible for dispatching to a callback service.
 *
 * @version $Rev$ $Date$
 */
public class JDKCallbackInvocationHandler<T> implements InvocationHandler {
    private final Class<T> interfaze;
    private final boolean conversational;
    private Map<String, Map<Method, InvocationChain>> mappings;

    public JDKCallbackInvocationHandler(Class<T> interfaze, boolean conversational, Map<String, Map<Method, InvocationChain>> mappings) {
        this.interfaze = interfaze;
        this.conversational = conversational;
        this.mappings = mappings;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        throw new UnsupportedOperationException();
    }

    private Object handleProxyMethod(Method method) throws TargetInvocationException {
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
        throw new TargetInvocationException("Operation not configured", method.getName());
    }

}
