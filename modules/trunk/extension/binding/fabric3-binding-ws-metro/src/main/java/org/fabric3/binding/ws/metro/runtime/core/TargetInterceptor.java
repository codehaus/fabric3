/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
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
import java.net.URL;

import javax.xml.ws.Service;

import org.fabric3.binding.ws.metro.provision.WsdlElement;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Interceptor for invoking web services.
 *
 */
public class TargetInterceptor implements Interceptor {
    
    private WsdlElement wsdlElement;
    private Class<?> sei;
    private URL[] referenceUrls;
    private ClassLoader classLoader;
    private Method method;
    

    /**
     * Initialises the instance state.
     * 
     * @param wsdlElement WSDL element contains the WSDL 1.1 port and service names.
     * @param sei Service endpoint interface.
     * @param referenceUrls URLs used to invoke the web service.
     * @param method Method to be invoked.
     */
    public TargetInterceptor(WsdlElement wsdlElement, Class<?> sei, URL[] referenceUrls, ClassLoader classLoader, Method method) {
        this.wsdlElement = wsdlElement;
        this.sei = sei;
        this.referenceUrls = referenceUrls;
        this.classLoader = classLoader;
        this.method = method;
    }

    /**
     * Gets the next interceptor in the chain.
     */
    public Interceptor getNext() {
        return null;
    }

    /**
     * Sets the next interceptor in the chain.
     */
    public void setNext(Interceptor next) {
    }

    /**
     * Invokes the web service.
     */
    public Message invoke(Message msg) {
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        
        try {
        
            Thread.currentThread().setContextClassLoader(classLoader);
            
            Service service = Service.create(referenceUrls[0], wsdlElement.getServiceName());
            Object proxy = service.getPort(sei);
            Object[] payload = (Object[]) msg.getBody();
            Object ret = method.invoke(proxy, payload[0]);
            
            return new MessageImpl(ret, false, null);
            
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            throw new AssertionError(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        
    }

}
