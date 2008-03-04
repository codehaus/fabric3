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
package org.fabric3.spring;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class SpringTargetInterceptor implements Interceptor {

    private Method method = null;
    private Object bean = null;
    
    Signature signature;
    SpringComponent<?> springComponent;

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    public SpringTargetInterceptor(Signature signature, SpringComponent<?> springComponent) {
        this.signature = signature;
        this.springComponent = springComponent;
    }
    
    public Message invoke(Message message) {
        Object bean = null;
        try {
            bean = springComponent.createObjectFactory().getInstance();
        } catch (ObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Message result = invoke(message, bean);

        return result;
    }

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

}
