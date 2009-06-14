/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 * --- Original Apache License ---
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.binding.ws.metro.runtime.core;

import java.net.URL;
import javax.servlet.ServletConfig;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import com.sun.xml.ws.transport.http.servlet.WSServlet;
import com.sun.xml.ws.transport.http.servlet.WSServletDelegate;

import org.fabric3.binding.ws.provision.WsdlElement;

/**
 * Servlet that handles all the incoming request, extends the Metro servlet and overrides the <code>getDelegate</code> method.
 */
public class MetroServlet extends WSServlet {
    private static final long serialVersionUID = -2581439830158433922L;

    private ServletAdapterFactory servletAdapterFactory = new ServletAdapterFactory();
    private F3ServletDelegate delegate;

    /**
     * Registers a new service.
     *
     * @param sei         Service end point interface.
     * @param wsdlUrl     Optional URL to the WSDL document.
     * @param servicePath Relative path on which the service is provisioned.
     * @param wsdlElement WSDL element that encapsulates the WSDL 1.1 service and port names.
     * @param invoker     Invoker for receiving the web service request.
     * @param features    Web service features to enable.
     * @param bindingID   Binding ID to use.
     */
    public void registerService(Class<?> sei,
                                URL wsdlUrl,
                                String servicePath,
                                WsdlElement wsdlElement,
                                F3Invoker invoker,
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
                                                         wsdlElement.getServiceName(),
                                                         wsdlElement.getPortName(),
                                                         null,
                                                         binding,
                                                         primaryWsdl,
                                                         null,
                                                         null,
                                                         true);

            ServletAdapter adapter = servletAdapterFactory.createAdapter(servicePath, servicePath, wsEndpoint);
            delegate.registerServletAdapter(adapter, seiClassLoader);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    /**
     * Gets the {@link WSServletDelegate} that we will be forwarding the requests to.
     *
     * @return Returns a Fabric3 servlet delegate.
     */
    protected WSServletDelegate getDelegate(ServletConfig servletConfig) {
        delegate = new F3ServletDelegate(servletConfig.getServletContext());
        return delegate;
    }

}
