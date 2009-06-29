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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.WSService;
import com.sun.xml.ws.wsdl.parser.InaccessibleWSDLException;
import com.sun.xml.wss.SecurityEnvironment;

import org.fabric3.host.work.WorkScheduler;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.classloader.MultiParentClassLoader;

/**
 * Lazily creates a service proxy that can be shared among invocation chains of a wire. The proxy must be lazily created as opposed to during wire
 * attachment as the JAX-WS runtime attempts to access the WSDL from the endpoint address, which may not be provisioned at that time.
 *
 * @version $Rev$ $Date$
 */
public class LazyProxyObjectFactory implements ObjectFactory<Object> {
    private URL wsdlLocation;
    private QName serviceName;
    private Class<?> seiClass;
    private WebServiceFeature[] features;
    private File wsitConfiguration;
    private WorkScheduler scheduler;
    private SecurityEnvironment securityEnvironment;
    private Object proxy;

    public LazyProxyObjectFactory(URL wsdlLocation,
                                  QName serviceName,
                                  Class<?> seiClass,
                                  WebServiceFeature[] features,
                                  File wsitConfiguration,
                                  WorkScheduler scheduler,
                                  SecurityEnvironment securityEnvironment) {
        this.wsdlLocation = wsdlLocation;
        this.serviceName = serviceName;
        this.seiClass = seiClass;
        this.features = features;
        this.wsitConfiguration = wsitConfiguration;
        this.scheduler = scheduler;
        this.securityEnvironment = securityEnvironment;
    }

    public Object getInstance() throws ObjectCreationException {
        if (proxy == null) {
            // there is a possibility more than one proxy will be created but since this does not have side-effects, avoid synchronization
            proxy = createProxy();
        }
        return proxy;
    }

    private Object createProxy() throws ObjectCreationException {
        // Metro requires library classes to be visibile to the application classloader. If executing in an environment that supports classloader
        // isolation, dynamically update the application classloader by setting a parent to the Metro classloader.
        ClassLoader seiClassLoader = seiClass.getClassLoader();
        if (seiClassLoader instanceof MultiParentClassLoader) {
            MultiParentClassLoader multiParentClassLoader = (MultiParentClassLoader) seiClassLoader;
            ClassLoader extensionCl = getClass().getClassLoader();
            if (!multiParentClassLoader.getParents().contains(extensionCl)) {
                multiParentClassLoader.addParent(extensionCl);
            }
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);
            Service service;
            WSService.InitParams params = new WSService.InitParams();
            WsitClientConfigurationContainer container;
            if (wsitConfiguration != null) {
                // Policy configured
                container = new WsitClientConfigurationContainer(wsitConfiguration, securityEnvironment);
            } else {
                // No policy
                container = new WsitClientConfigurationContainer(securityEnvironment);
            }
            params.setContainer(container);
            service = WSService.create(wsdlLocation, serviceName, params);
            // use the kernel scheduler for dispatching
            service.setExecutor(scheduler);
            return service.getPort(seiClass, features);
        } catch (InaccessibleWSDLException e) {
            throw new ObjectCreationException(e);
        } catch (MalformedURLException e) {
            throw new ObjectCreationException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

}
