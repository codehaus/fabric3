package org.fabric3.binding.ws.jaxws.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.MalformedURLException;

import javax.xml.ws.Service;
import javax.xml.namespace.QName;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

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

public class WsTargetInterceptor implements Interceptor {

    private final Method method;
    private final Class interfazz;
    private Interceptor next;
    private Object reference;
    private final QName portQName;
    private final QName serviceQName;
    private final String wsdlLocation;
    private Service service;

    public WsTargetInterceptor(Method method,
                               Class interfazz,
                               String wsdlLocation,
                               String serviceName,
                               String portName,
                               String targetNamespace) {
        this.method = method;
        this.interfazz = interfazz;
        this.wsdlLocation = wsdlLocation;
        this.portQName = new QName(targetNamespace, portName);
        this.serviceQName = new QName(targetNamespace, serviceName);
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {
        Object object = getReference();
        Object[] parameters = (Object[]) message.getBody();
        Message result = new MessageImpl();
        try {
            result.setBody(method.invoke(object, parameters));
        } catch (InvocationTargetException ite) {
            reference = null;
            result.setBodyWithFault(ite.getCause());
        } catch (Exception e) {
            reference = null;
            throw new ServiceRuntimeException(e);
        }
        return result;
    }


    private Object getReference() {
        if (service == null) {
            try {
                service = Service.create(new URL(wsdlLocation), serviceQName);
            } catch (MalformedURLException mue) {
                AssertionError ae = new AssertionError("Unexpected exception");
                ae.initCause(mue);
                throw ae;
            }
        }
        if (reference == null) {
            reference = service.getPort(portQName, interfazz);
        }
        return reference;
    }

}
