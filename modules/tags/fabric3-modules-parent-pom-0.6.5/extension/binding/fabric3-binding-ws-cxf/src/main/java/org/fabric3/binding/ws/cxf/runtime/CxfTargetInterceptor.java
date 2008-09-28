/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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
package org.fabric3.binding.ws.cxf.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osoa.sca.ServiceUnavailableException;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

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
     * @param proxy  Proxy to the target.
     */
    public CxfTargetInterceptor(Method method, Object proxy) {
        this.method = method;
        this.proxy = proxy;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {

        Object[] args = (Object[]) message.getBody();
        try {
            Object ret = method.invoke(proxy, args);
            Message result = new MessageImpl();
            result.setBody(ret);
            return result;
        } catch (IllegalAccessException e) {
            throw new ServiceUnavailableException(e);
        } catch (InvocationTargetException e) {
            // FIXME CXF throws a SoapFault unchecked exception with a null cause rather than the fault raised by the implementation
            Message result = new MessageImpl();
            result.setBodyWithFault(e.getCause());
            return result;
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
