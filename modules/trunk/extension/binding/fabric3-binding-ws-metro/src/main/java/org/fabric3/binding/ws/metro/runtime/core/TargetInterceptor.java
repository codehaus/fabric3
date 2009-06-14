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
package org.fabric3.binding.ws.metro.runtime.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;

import org.fabric3.binding.ws.provision.WsdlElement;
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
    private WebServiceFeature[] features;
    
    private Random random = new Random();
    private List<URL> failedUrls = new LinkedList<URL>();
    

    /**
     * Initialises the instance state.
     * 
     * @param wsdlElement WSDL element contains the WSDL 1.1 port and service names.
     * @param sei Service endpoint interface.
     * @param referenceUrls URLs used to invoke the web service.
     * @param method Method to be invoked.
     * @param features Features to enable.
     * @param bindingID Binding ID to use.
     */
    public TargetInterceptor(WsdlElement wsdlElement, 
                             Class<?> sei, 
                             URL[] referenceUrls, 
                             ClassLoader classLoader, 
                             Method method, 
                             WebServiceFeature[] features) {
        this.wsdlElement = wsdlElement;
        this.sei = sei;
        this.referenceUrls = referenceUrls;
        this.classLoader = classLoader;
        this.method = method;
        this.features = features;
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
        
        URL endpointUrl = getEndpointUrl();
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        
        try {
        
            Thread.currentThread().setContextClassLoader(classLoader);

            Service service = Service.create(endpointUrl, wsdlElement.getServiceName());
            Object proxy = service.getPort(sei, features);
            Object[] payload = (Object[]) msg.getBody();
            Object ret = method.invoke(proxy, payload);
            
            failedUrls.clear();
            return new MessageImpl(ret, false, null);
            
        } catch (InaccessibleWSDLException e) {
            failedUrls.add(endpointUrl);
            if (failedUrls.size() != referenceUrls.length) {
                return invoke(msg);
            } else {
                throw e;
            }
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            return new MessageImpl(e.getTargetException(), true, null);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
        
    }

    /*
     * Gets the endpoint url
     */
    private URL getEndpointUrl() {

        int index = random.nextInt(referenceUrls.length);
        URL endpointUrl = referenceUrls[index];

        if (failedUrls.contains(endpointUrl)) {
            endpointUrl = getEndpointUrl();
        }

        return endpointUrl;

    }

}
