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
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Interceptor for invoking web services.
 */
public class TargetInterceptor implements Interceptor {
    private QName serviceName;
    private Class<?> seiClass;
    private URL targetUrl;
    private ClassLoader classLoader;
    private Method method;
    private WebServiceFeature[] features;
    private Object proxy;


    /**
     * Constructor.
     *
     * @param serviceName the target service name
     * @param seiClass    service endpoint interface
     * @param targetUrl   URL of the target the web service
     * @param classLoader the classloader with visibility to application parameter types
     * @param method      method corresponding to the invoked operation
     * @param features    web service features to enable
     */
    public TargetInterceptor(QName serviceName,
                             Class<?> seiClass,
                             URL targetUrl,
                             ClassLoader classLoader,
                             Method method,
                             WebServiceFeature[] features) {
        this.serviceName = serviceName;
        this.seiClass = seiClass;
        this.targetUrl = targetUrl;
        this.classLoader = classLoader;
        this.method = method;
        this.features = features;
    }

    public Message invoke(Message msg) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            if (proxy == null) {
                // no synchronization since creating multiple proxies doesn't have side-effects and this avoids the later cost of a synchronized block
                Service service = Service.create(targetUrl, serviceName);
                proxy = service.getPort(seiClass, features);
            }
            Object[] payload = (Object[]) msg.getBody();
            Object ret = method.invoke(proxy, payload);
            return new MessageImpl(ret, false, null);
        } catch (InaccessibleWSDLException e) {
            throw new ServiceRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            return new MessageImpl(e.getTargetException(), true, null);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public Interceptor getNext() {
        return null;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last in the chain");
    }

}
