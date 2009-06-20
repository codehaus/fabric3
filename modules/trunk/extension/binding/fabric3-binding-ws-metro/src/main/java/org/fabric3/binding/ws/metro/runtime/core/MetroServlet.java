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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.URL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.mex.server.MEXEndpoint;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import org.fabric3.host.work.WorkScheduler;

/**
 * Handles incoming HTTP requests and dispatches them to the Metro stack. Extends the Metro servlet and overrides the <code>getDelegate</code>
 * method.
 *
 * @version $Rev$ $Date$
 */
public class MetroServlet extends WSServlet {
    private static final long serialVersionUID = -2581439830158433922L;
    private static final String MEX_SUFFIX = "/mex";

    private WorkScheduler scheduler;

    private ServletAdapterFactory servletAdapterFactory = new ServletAdapterFactory();
    private volatile F3ServletDelegate delegate;
    private F3Container container;
    private WSEndpoint<?> mexEndpoint;

    /**
     * Constructor
     *
     * @param scheduler the work scheduler for dispatching invocations.
     */
    public MetroServlet(WorkScheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);
        ServletContext servletContext = servletConfig.getServletContext();
        // Setup the WSIT endpoint that handles WS-MEX requests for registered endpoints. The TCCL must be set for JAXB.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader seiClassLoader = MEXEndpoint.class.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);
            container = new F3Container(servletContext);

            WSBinding binding = BindingImpl.create(BindingID.SOAP12_HTTP);
            mexEndpoint = WSEndpoint.create(MEXEndpoint.class,
                                            false,
                                            null,
                                            null,
                                            null,
                                            container,
                                            binding,
                                            null,
                                            null,
                                            null,
                                            true);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    /**
     * Registers a new service endpoint.
     *
     * @param sei         service endpoint interface.
     * @param serviceName service name
     * @param portName    port name
     * @param wsdlUrl     Optional URL to the WSDL document.
     * @param servicePath Relative path on which the service is provisioned.
     * @param invoker     Invoker for receiving the web service request.
     * @param features    Web service features to enable.
     * @param bindingID   Binding ID to use.
     */
    public void registerService(Class<?> sei,
                                QName serviceName,
                                QName portName,
                                URL wsdlUrl,
                                String servicePath,
                                MetroServiceInvoker invoker,
                                WebServiceFeature[] features,
                                BindingID bindingID) {

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        ClassLoader seiClassLoader = sei.getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(seiClassLoader);

            SDDocumentSource primaryWsdl = null;
            if (wsdlUrl != null) {
                primaryWsdl = SDDocumentSource.create(wsdlUrl);
            }

            WSBinding binding = BindingImpl.create(bindingID, features);

            WSEndpoint<?> wsEndpoint = WSEndpoint.create(sei,
                                                         false,
                                                         invoker,
                                                         serviceName,
                                                         portName,
                                                         container,
                                                         binding,
                                                         primaryWsdl,
                                                         null,
                                                         null,
                                                         true);
            wsEndpoint.setExecutor(scheduler);
            ServletAdapter adapter = servletAdapterFactory.createAdapter(servicePath, servicePath, wsEndpoint);
            delegate.registerServletAdapter(adapter, seiClassLoader);

            String mexPath = servicePath + MEX_SUFFIX;
            ServletAdapter mexAdapter = servletAdapterFactory.createAdapter(mexPath, mexPath, mexEndpoint);
            delegate.registerServletAdapter(mexAdapter, seiClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    /**
     * Unregisters a service endpoint.
     *
     * @param path the endpoint path
     */
    public void unregisterService(String path) {
        ServletAdapter adapter = delegate.unregisterServletAdapter(path);
        if (adapter != null) {
            container.removeEndpoint(adapter);
        }
        String mexPath = path + MEX_SUFFIX;
        servletAdapterFactory.createAdapter(mexPath, mexPath, mexEndpoint);
    }

    /**
     * Gets the {@link WSServletDelegate} that we will be forwarding the requests to.
     *
     * @return Returns a Fabric3 servlet delegate.
     */
    protected WSServletDelegate getDelegate(ServletConfig servletConfig) {
        if (delegate == null) {
            synchronized (this) {
                if (delegate == null) {
                    delegate = new F3ServletDelegate(servletConfig.getServletContext());
                }
            }
        }
        return delegate;
    }

}
