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

package org.fabric3.binding.ws.cxf;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.fabric3.extension.component.SimpleWorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.fabric3.spi.wire.Wire;

/**
 * Proxy handler for the invocation.
 *
 * @version $Revision$ $Date$
 */
public class ServiceProxyHandler implements InvocationHandler {

    /**
     * Wire attached to the servlet.
     */
    private Wire wire;

    /**
     * Map of op names to head interceptors.
     */
    private Map<String, InvocationChain> invocationChains;

    /*
    * Creates a new proxy handler.
    */
    private ServiceProxyHandler(Wire wire, Map<String, InvocationChain> invocationChains) {
        this.wire = wire;
        this.invocationChains = invocationChains;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Interceptor head = invocationChains.get(methodName).getHeadInterceptor();
        Message input = new MessageImpl(args, false, new SimpleWorkContext(), wire);
        Message output = head.invoke(input);
        if (output.isFault()) {
            throw (Throwable) output.getBody();
        } else {
            return output.getBody();
        }
    }

    /**
     * Creates a new proxy instance.
     *
     * @param <T>              the interface type
     * @param intf             Service interface.
     * @param invocationChains Map of op names to head interceptors.
     * @param wire             Wire that connects the transport to the component.
     * @return Proxied service.
     */
    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> intf, Map<String, InvocationChain> invocationChains, Wire wire) {
        ClassLoader cl = intf.getClassLoader();
        InvocationHandler handler = new ServiceProxyHandler(wire, invocationChains);
        return (T) Proxy.newProxyInstance(cl, new Class<?>[]{intf}, handler);
    }

}
