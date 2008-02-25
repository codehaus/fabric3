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

import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.spi.component.CallFrame;
import org.fabric3.spi.component.TargetInvocationException;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;

/**
 * Responsible for dispatching to a callback service from a component implementation instance that is not composite scope. Callback URIs and
 * conversation ids can be cached for all scopes other than composite as only one client can invoke the instance at a time.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class StatefullCallbackInvocationHandler<T> implements InvocationHandler {
    private final Class<T> interfaze;
    private final Object conversationId;
    private final String callbackUri;
    private Map<Method, InvocationChain> chains;

    /**
     * Constructor.
     *
     * @param interfaze      the callback service interface implemented by the proxy
     * @param conversationId the conversation id for the callback service
     * @param callbackUri    the callback target URI;
     * @param chains         the invocation chain mappings for the callback wire
     */
    public StatefullCallbackInvocationHandler(Class<T> interfaze,
                                              Object conversationId,
                                              String callbackUri,
                                              Map<Method, InvocationChain> chains) {
        // needed to implement ServiceReference
        this.interfaze = interfaze;
        // cache the conversation id and callback URI as instance variables since this proxy only invokes a single callback instance
        this.conversationId = conversationId;
        this.chains = chains;
        this.callbackUri = callbackUri;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WorkContext workContext = PojoWorkContextTunnel.getThreadWorkContext();
        // pop the call frame as we move back in the request stack. When the invocation is made on the callback target, the same call frame state
        // will be present as existed when the initial forward request to this proxy's instance was dispatched to. Consequently,
        // CallFrame#getForwardCorrelaltionId() will return the correlation id for the callback target.
        CallFrame frame = workContext.popCallFrame();
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }
        Interceptor headInterceptor = chain.getHeadInterceptor();
        assert headInterceptor != null;

        // send the invocation down the wire
        Message msg = new MessageImpl();
        msg.setBody(args);
        msg.setWorkContext(workContext);
        try {
            // dispatch the wire down the chain and get the response
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

            // handle response from the application, returning or throwing is as appropriate
            Object body = resp.getBody();
            if (resp.isFault()) {
                throw (Throwable) body;
            } else {
                return body;
            }
        } finally {
            // push the call frame for this component instance back onto the stack
            workContext.addCallFrame(frame);
        }
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