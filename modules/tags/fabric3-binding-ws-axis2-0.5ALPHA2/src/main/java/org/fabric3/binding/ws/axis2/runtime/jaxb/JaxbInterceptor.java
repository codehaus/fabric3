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
package org.fabric3.binding.ws.axis2.runtime.jaxb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.axiom.om.OMElement;

import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;

/**
 * Interceptor that transforms an OMElement to a JAXB bound object on the way in and JAXB bound object to OMElement on the way out.
 * <p/>
 * TODO The interceptor assumes doc-lit wrapped, this may need to be fixed.
 *
 * @version $Revision$ $Date$
 */
public class JaxbInterceptor implements Interceptor {

    private Interceptor next;
    private final ClassLoader classLoader;
    private final OMElement2Jaxb inTransformer;
    private final Jaxb2OMElement outTransformer;
    private final boolean service;
    private final Map<Class<?>, Constructor<?>> faultMapping;

    public JaxbInterceptor(ClassLoader classLoader, JAXBContext jaxbContext, boolean service, Map<Class<?>, Constructor<?>> faultMapping) throws JAXBException {
        this.classLoader = classLoader;
        inTransformer = new OMElement2Jaxb(jaxbContext);
        outTransformer = new Jaxb2OMElement(jaxbContext);
        this.service = service;
        this.faultMapping = faultMapping;
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            return service ? interceptService(message) : interceptReference(message);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }

    }

    private Message interceptService(Message message) {

        Object[] payload = (Object[]) message.getBody();

        if (payload != null && payload.length > 0) {
            OMElement omElement = (OMElement) payload[0];
            Object jaxbObject = inTransformer.transform(omElement, null);
            message.setBody(new Object[]{jaxbObject});
        }

        Message response = next.invoke(message);
        Object result;
        if (response.isFault()) {
            Object webFault = response.getBody();
            result = getFault(webFault);
        } else {
            result = response.getBody();
        }

        if (result != null) {
            OMElement omElement = outTransformer.transform(result, null);
            response.setBody(omElement);
        }

        return response;

    }

    private Object getFault(Object webFault) {
        try {
            Method getFaultInfo = webFault.getClass().getMethod("getFaultInfo");
            return getFaultInfo.invoke(webFault);
        } catch (Exception e) {
            // should not occur as the user class is only meant to be a getter and the generator should have checked the signature
            throw new AssertionError(e);
        }
    }

    private Message interceptReference(Message message) {

        Object[] payload = (Object[]) message.getBody();

        if (payload != null && payload.length > 0) {
            Object jaxbObject = payload[0];
            OMElement omElement = outTransformer.transform(jaxbObject, null);
            message.setBody(new Object[]{omElement});
        }

        Message response = next.invoke(message);
        Object result = response.getBody();

        if (result != null) {
            OMElement omElement = (OMElement) result;
            Object jaxbObject = inTransformer.transform(omElement, null);
            Constructor<?> faultConstructor = faultMapping.get(jaxbObject.getClass());
            if (faultConstructor != null) {
                // the received message maps to a fault
                try {
                    Object fault = faultConstructor.newInstance(null, jaxbObject);
                    response.setBodyWithFault(fault);
                } catch (InstantiationException e) {
                    throw new AssertionError();
                } catch (IllegalAccessException e) {
                    throw new AssertionError();
                } catch (InvocationTargetException e) {
                    throw new AssertionError();
                }
            } else {
                // the received message does not map to a fault so assume it's a normal response
                response.setBody(jaxbObject);
            }
        }

        return response;

    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
