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

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback service from multi-threaded component instances such as composite scope components. Since callback
 * proxies for multi-threaded components may dispatch to multiple callback services, this implementation must determine the correct target service
 * based on the current CallFrame. For example, if clients A and A' implementing the same callback interface C invoke B, the callback proxy
 * representing C must correctly dispatch back to A and A'. This is done by recording the callback URI in the current CallFrame as the forward invoke
 * is made.
 *
 * @version $Rev: 3150 $ $Date: 2008-03-21 14:12:51 -0700 (Fri, 21 Mar 2008) $
 */
public class MultiThreadedCallbackInvocationHandler<T> extends AbstractCallbackInvocationHandler<T> {
    private Map<String, Map<Method, InvocationChain>> mappings;

    /**
     * Constructor. In multi-threaded instances such as composite scoped components, multiple forward invocations may be received simultaneously. As a
     * result, since callback proxies stored in instance variables may represent multiple clients, they must map the correct one for the request being
     * processed on the current thread. The mappings parameter keys a callback URI representing the client to the set of invocation chains for the
     * callback service.
     *
     * @param interfaze the callback service interface implemented by the proxy
     * @param mappings  the callback URI to invocation chain mappings
     */
    public MultiThreadedCallbackInvocationHandler(Class<T> interfaze, Map<String, Map<Method, InvocationChain>> mappings) {
        super(interfaze);
        this.mappings = mappings;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
        CallFrame frame = workContext.peekCallFrame();
        String callbackUri = frame.getCallbackUri();
        Map<Method, InvocationChain> chains = mappings.get(callbackUri);
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains.get(method);
        // find the invocation chain for the invoked operation
        if (chain == null) {
            return handleProxyMethod(method);
        }
        return super.invoke(chain, args, workContext);
    }

}
