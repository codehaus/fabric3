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
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback service from a component implementation instance that is not composite scope. Since only one client can
 * invoke the instance this proxy is injected on at a time, there can only be one callback target, even if the proxy is injected on an instance
 * variable. Consequently, the proxy does not need to map the callback target based on the forward request.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class StatefulCallbackInvocationHandler<T> extends AbstractCallbackInvocationHandler<T> {
    private Map<Method, InvocationChain> chains;
    private ScopeContainer scopeContainer;

    /**
     * Constructor.
     *
     * @param interfaze the callback service interface implemented by the proxy
     * @param chains    the invocation chain mappings for the callback wire
     */
    public StatefulCallbackInvocationHandler(Class<T> interfaze, Map<Method, InvocationChain> chains) {
        super(interfaze);
        this.chains = chains;
    }

    /**
     * Constructor.
     *
     * @param interfaze      the callback service interface implemented by the proxy
     * @param scopeContainer the conversational scope container
     * @param chains         the invocation chain mappings for the callback wire
     */
    public StatefulCallbackInvocationHandler(Class<T> interfaze, ScopeContainer<?> scopeContainer, Map<Method, InvocationChain> chains) {
        super(interfaze);
        this.scopeContainer = scopeContainer;
        this.chains = chains;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }
        try {
            return super.invoke(chain, args, workContext);
        } finally {
            if (chain.getPhysicalOperation().isEndsConversation()) {
                scopeContainer.stopContext(workContext);
            }
        }
    }

}