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
import java.util.Map;
import javax.xml.ws.BindingProvider;

import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.binding.ws.metro.provision.SecurityConfiguration;
import org.fabric3.binding.ws.metro.runtime.MetroConstants;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.wire.Interceptor;

/**
 * Interceptor for invoking a web service proxy.
 *
 * @version $Rev$ $Date$
 */
public class MetroTargetInterceptor implements Interceptor {
    private ObjectFactory<?> proxyFactory;
    private Method method;
    private SecurityConfiguration configuration;

    /**
     * Constructor.
     *
     * @param proxyFactory  the service proxy factory
     * @param method        method corresponding to the invoked operation
     * @param configuration the security configuration or null if security is not configured
     */
    public MetroTargetInterceptor(ObjectFactory<?> proxyFactory, Method method, SecurityConfiguration configuration) {
        this.proxyFactory = proxyFactory;
        this.method = method;
        this.configuration = configuration;
    }

    public Message invoke(Message msg) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Object[] payload = (Object[]) msg.getBody();
            Object proxy = proxyFactory.getInstance();
            configureSecurity(((BindingProvider) proxy));
            // Metro stubs attempt to load classes using TCCL (e.g. StAX provider classes) that are visible the extension classloader and not
            // visible to the application classloader.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            Object ret = method.invoke(proxy, payload);
            return new MessageImpl(ret, false, null);
        } catch (InaccessibleWSDLException e) {
            throw new ServiceRuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new AssertionError(e);
        } catch (InvocationTargetException e) {
            return new MessageImpl(e.getTargetException(), true, null);
        } catch (ObjectCreationException e) {
            throw new ServiceRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Configures the outbound security context.
     *
     * @param provider the binding provider for the invocation
     */
    private void configureSecurity(BindingProvider provider) {
        if (configuration == null) {
            // no security
            return;
        }
        if (configuration.getUsername() != null) {
            // User authentication configured
            // Places authentication information in the invocation context, which is used by the Fabric3 security environment to include the
            // credentials in the message header.
            Map<String, Object> context = provider.getRequestContext();
            context.put(MetroConstants.USERNAME, configuration.getUsername());
            context.put(MetroConstants.PASSWORD, configuration.getPassword());
        }

    }

    public Interceptor getNext() {
        return null;
    }

    public void setNext(Interceptor next) {
        throw new IllegalStateException("This interceptor must be the last in the chain");
    }

}
