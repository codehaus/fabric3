  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.binding.ws.axis2.runtime.jaxb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.WebFault;

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
        
        WebFault annotation = webFault.getClass().getAnnotation(WebFault.class);
        if (annotation == null) {
            // this is an undeclared exception
            if (webFault instanceof RuntimeException) {
                throw (RuntimeException) webFault;
            } else if (webFault instanceof Exception) {
                throw new AssertionError((Exception) webFault);
            }
        }
        
        try {
            Method getFaultInfo = webFault.getClass().getMethod("getFaultInfo");
            return getFaultInfo.invoke(webFault);
        } catch (Exception e) {
            throw new AssertionError(e);
        }

    }

    private Message interceptReference(Message message) {
        
        System.err.println("Intercepting reference");

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
