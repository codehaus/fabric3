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
package org.fabric3.binding.ejb.transport;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.ejb.wire.EjbResolver;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public abstract class BaseEjbTargetInterceptor implements Interceptor {

    protected final EjbResolver resolver;
    protected final Signature signature;
    private Method method = null;

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    
    public BaseEjbTargetInterceptor(Signature signature, EjbResolver resolver) {
        this.signature = signature;
        this.resolver = resolver;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

    public abstract Message invoke(Message message);

    protected Message invoke(Message message, Object ejb) {
        Object[] parameters = (Object[]) message.getBody();

        if(method == null) {

            try {
                method = signature.getMethod(ejb.getClass());
            } catch(ClassNotFoundException cnfe) {
                throw new ServiceRuntimeException(cnfe);
            } catch(NoSuchMethodException nsme) {
                //TODO Give better error message
                throw new ServiceRuntimeException("The method "+signature+
                        " did not match any methods on the interface of the Target");
            }

        }


        Message result = new MessageImpl();
        try {
            result.setBody(method.invoke(ejb, parameters));
        } catch(InvocationTargetException ite) {
           result.setBodyWithFault(ite.getCause());
        } catch(Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }
    
}
