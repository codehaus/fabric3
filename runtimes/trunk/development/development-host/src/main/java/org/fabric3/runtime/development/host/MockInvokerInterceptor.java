/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.runtime.development.host;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;

/**
 * Invokes a mock reference object
 *
 * @version $Rev$ $Date$
 */
public class MockInvokerInterceptor implements Interceptor {
    private Method method;
    private Object mock;

    public MockInvokerInterceptor(Method method, Object mock) {
        this.method = method;
        this.mock = mock;
    }

    public Message invoke(Message msg) {
        try {
            Object ret = method.invoke(mock, (Object[]) msg.getBody());
            msg.setBody(ret);
        } catch (IllegalAccessException e) {
            msg.setBodyWithFault(e);
        } catch (InvocationTargetException e) {
            msg.setBodyWithFault(e);
        }
        return msg;
    }

    public void setNext(Interceptor next) {
        throw new UnsupportedOperationException();
    }

    public Interceptor getNext() {
        return null;
    }
}
