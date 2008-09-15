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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.rmi.wire.RmiReferenceFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

public final class RmiTargetInterceptor implements Interceptor {

    private final RmiReferenceFactory factory;
    private final Method method;
    private Interceptor next;

    public RmiTargetInterceptor(Method method, RmiReferenceFactory factory) {
        this.method = method;
        this.factory = factory;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {
        Object object = factory.getReference();
        Object[] parameters = (Object[]) message.getBody();
        Message result = new MessageImpl();
        try {
            result.setBody(Proxy.getInvocationHandler(object).invoke(object, method, parameters));
//            result.setBody(method.invoke(object, parameters));
        } catch (InvocationTargetException ite) {
            result.setBodyWithFault(ite.getCause());
        } catch (Throwable e) {
            if (e instanceof Error) {
                // re-throw an error since it should not be caught
                throw (Error) e;
            }
            throw new ServiceRuntimeException(e);
        }
        return result;
    }

}
