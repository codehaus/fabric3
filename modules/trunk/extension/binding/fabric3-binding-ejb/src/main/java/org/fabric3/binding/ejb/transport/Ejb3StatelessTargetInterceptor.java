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

import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.ejb.wire.EjbReferenceFactory;
import org.fabric3.spi.wire.Message;
import org.fabric3.spi.wire.MessageImpl;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class Ejb3StatelessTargetInterceptor extends BaseEjbTargetInterceptor {


    /**
     * Initializes the reference URL.
     */
    public Ejb3StatelessTargetInterceptor(String methodName, EjbReferenceFactory referenceFactory) {
        super(methodName, referenceFactory);
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.wire.Message)
     */
    public Message invoke(Message message) {
        Object slsb = referenceFactory.getEjbReference();

        Object[] parameters = (Object[]) message.getBody();

        if(method == null) {
            Class iface = slsb.getClass();
            Class[] parameterTypes = new Class[parameters.length];
            for(int i=0; i<parameters.length; i++) {
                parameterTypes[i] = parameters[i].getClass();
            }

            try {
                method = iface.getMethod(methodName, parameterTypes);
            } catch(NoSuchMethodException nsme) {
                //TODO Give them a better error message 
                throw new ServiceRuntimeException("The method "+methodName+
                        " did not match any methods on the interface of the Target");
            }

        }


        Message result = new MessageImpl();
        try {
            result.setBody(method.invoke(slsb, parameters));
        } catch(InvocationTargetException ite) {
           result.setBodyWithFault(ite.getCause());
        } catch(Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }


}
