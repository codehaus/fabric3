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
package org.fabric3.binding.ws.cxf.wire;

import java.lang.reflect.Method;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;
import org.osoa.sca.ServiceUnavailableException;

/**
 * @version $Revision: 1589 $ $Date: 2007-10-25 23:13:37 +0100 (Thu, 25 Oct 2007) $
 */
public class CxfTargetInterceptor implements Interceptor {

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    /**
     * Reference URL
     */
    private Method method;

    /**
     * Proxy.
     */
    private Object proxy;

    /**
     * Initializes the target method and proxy
     *
     * @param method Target method.
     * @param proxy Proxy to the target.
     */
    public CxfTargetInterceptor(Method method, Object proxy) {
        this.method = method;
        this.proxy = proxy;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {

        try {

            Message result = new MessageImpl();
            Object[] args = (Object[]) message.getBody();
            Object ret = method.invoke(proxy, args);
            result.setBody(ret);
            return result;

        } catch (Exception ex) {
            throw new ServiceUnavailableException(ex);
        }

    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
