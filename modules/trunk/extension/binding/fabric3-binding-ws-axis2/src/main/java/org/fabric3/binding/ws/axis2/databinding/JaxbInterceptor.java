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
package org.fabric3.binding.ws.axis2.databinding;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;

/**
 * Interceptor that transforms an OMElement to a JAXB bound object on the way in 
 * and JAXB bound object to OMElement on the way out.
 * 
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

    public JaxbInterceptor(ClassLoader classLoader, List<Class<?>> classes, boolean service) {
        this.classLoader = classLoader;
        inTransformer = new OMElement2Jaxb(classes);
        outTransformer = new Jaxb2OMElement(classes);
        this.service = service;
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
            message.setBody(new Object[] {jaxbObject});
        }
            
        Message response = next.invoke(message);
        Object result = response.getBody();
        
        if (result != null) {
            OMElement omElement = outTransformer.transform(result, null);
            response.setBody(omElement);
        }
            
        return response;
        
    }

    private Message interceptReference(Message message) {
        
        Object[] payload = (Object[]) message.getBody();
        
        if (payload != null && payload.length > 0) {
            Object jaxbObject = payload[0];
            OMElement omElement = outTransformer.transform(jaxbObject, null);
            message.setBody(new Object[] {omElement});
        }
            
        Message response = next.invoke(message);
        Object result = response.getBody();
        
        if (result != null) {        
            OMElement omElement = (OMElement) result;
            Object jaxbObject = inTransformer.transform(omElement, null);        
            response.setBody(jaxbObject);
        }
            
        return response;
        
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
