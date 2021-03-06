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
package org.fabric3.binding.ws.metro.runtime.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.ws.WebServiceContext;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.Invoker;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;


/**
 * Invoker that passes the incoming invocation through the interceptor chain.
 */
public class F3Invoker extends Invoker {

    private Map<String, InvocationChain> invocationChains = new HashMap<String, InvocationChain>();

    /**
     * Instantiates the invocation chains.
     *
     * @param invocationChains Invocation chains.
     */
    public F3Invoker(List<InvocationChain> invocationChains) {
        for (InvocationChain chain : invocationChains) {
            this.invocationChains.put(chain.getPhysicalOperation().getName(), chain);
        }
    }

    /**
     * Overridden as the super class method throws <code>UnsupportedOperationException</code>
     */
    @Override
    public void start(WebServiceContext wsc) {
    }

    /**
     * Invokes the head interceptor.
     */
    public Object invoke(Packet packet, Method method, Object... args) throws InvocationTargetException {

        Interceptor head = invocationChains.get(method.getName()).getHeadInterceptor();
        WorkContext workContext = new WorkContext();
        // TODO Add any header tunnelling

        Message input = new MessageImpl(args, false, workContext);
        Message ret = head.invoke(input);

        if (!ret.isFault()) {
            return ret.getBody();
        } else {
            Throwable th = (Throwable) ret.getBody();
            throw new InvocationTargetException(th);
        }

    }

}
