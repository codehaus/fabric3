/*
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
package org.fabric3.runtime.webapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.runtime.webapp.ServletRequestInjector;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.host.ServletHost;

/**
 * A <code>ServletHost</code> implementation that forwards requests to registered servlets
 *
 * @version $Rev$ $Date$
 */
@Service(ServletHost.class)
@EagerInit
public class ServletHostImpl implements ServletHost, ServletRequestInjector {
    protected Map<String, Servlet> servlets;
    protected ScopeRegistry registry;

    public ServletHostImpl() {
        this.servlets = new HashMap<String, Servlet>();
    }

    @Reference(required = false)
    public void setSessionScopeContainer(ScopeRegistry registry) {
        this.registry = registry;
    }

    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        assert req instanceof HttpServletRequest : "implementation only supports HttpServletRequest";
        String path = ((HttpServletRequest) req).getPathInfo();
        Servlet servlet = servlets.get(path);
        if (servlet == null) {
            throw new IllegalStateException("No servlet registered for path: " + path);
        }
        servlet.service(req, resp);
    }

    public void registerMapping(String path, Servlet servlet) {
        if (servlets.containsKey(path)) {
            throw new IllegalStateException("Servlet already registered at path: " + path);
        }
        servlets.put(path, servlet);
    }

    public boolean isMappingRegistered(String mapping) {
        return servlets.containsKey(mapping);

    }

    public Servlet unregisterMapping(String path) {
        return servlets.remove(path);
    }

}