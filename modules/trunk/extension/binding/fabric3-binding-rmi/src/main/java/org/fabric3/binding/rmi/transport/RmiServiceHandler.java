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
package org.fabric3.binding.rmi.transport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

public class RmiServiceHandler implements InvocationHandler {

    /**
     * Map of op names to operation definitions.
     */
    private final Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;


    public RmiServiceHandler(Map<Method, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops) {
        this.ops = ops;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //FIXME prasad@bea.com Handle equals
        if (method.getName().equals("equals")
                && method.getParameterTypes().length == 1
                && (method.getParameterTypes()[0]).equals(Object.class)) {
            if (!Proxy.isProxyClass(args[0].getClass())) {
                return false;
            }
            InvocationHandler h = Proxy.getInvocationHandler(args[0]);
            return this.equals(h);
        }

        Interceptor head = ops.get(method).getValue().getHeadInterceptor();

        Message input = new MessageImpl(args, false, new WorkContext());

        Message output = head.invoke(input);
        if (output.isFault()) {
            throw (Throwable) output.getBody();
        } else {
            return output.getBody();
        }
    }
}
