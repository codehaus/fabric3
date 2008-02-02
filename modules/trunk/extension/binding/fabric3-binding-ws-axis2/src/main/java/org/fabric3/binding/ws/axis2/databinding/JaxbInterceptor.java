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
import org.fabric3.spi.wire.MessageImpl;

/**
 * Interceptor that transforms an OMElement to a JAXB bound object on the way in 
 * and JAXB bound object to OMElement on the way out.
 * 
 * @version $Revision$ $Date$
 */
public class JaxbInterceptor implements Interceptor {
    
    private Interceptor next;
    private ClassLoader classLoader;
    private OMElement2Jaxb inTransformer;
    private Jaxb2OMElement outTransformer;

    public JaxbInterceptor(ClassLoader classLoader, List<Class<?>> inClasses, Class<?> outClass) {
        this.classLoader = classLoader;
        inTransformer = new OMElement2Jaxb(inClasses);
        outTransformer = new Jaxb2OMElement(outClass);
    }

    public Interceptor getNext() {
        return next;
    }

    public Message invoke(Message message) {
        
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        
        try {
            
            Thread.currentThread().setContextClassLoader(classLoader);
            
            OMElement inOmElement = (OMElement) message.getBody();
            Object inJaxbObject = inTransformer.transform(inOmElement, null);
            
            Message transformedMessage = new MessageImpl();
            transformedMessage.setBody(inJaxbObject);
            
            Message response = next.invoke(transformedMessage);
            Object outJaxbObject = response.getBody();
            OMElement outOmElement = outTransformer.transform(outJaxbObject, null);
            
            transformedMessage = new MessageImpl();
            transformedMessage.setBody(outOmElement);
            
            return transformedMessage;
            
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

}
