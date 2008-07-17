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
import java.util.Map;
import java.util.HashMap;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.host.ServletHost;

/**
 * A <code>ServletHost</code> implementation that forwards requests to registered servlets
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {ServletHost.class, ServletRequestInjector.class})
@EagerInit
public class ServletHostImpl implements ServletHost, ServletRequestInjector {
    protected Map<String, Servlet> servlets;
    protected ScopeRegistry registry;
    private WebappHostInfo info;

    public ServletHostImpl(@Reference WebappHostInfo info) {
        this.info = info;
        servlets = new HashMap<String, Servlet>();
    }

    public ServletContext getServletContext() {
        return info.getServletContext();
    }

    public void init(ServletConfig config) throws ServletException {
        for (Servlet servlet : servlets.values()) {
            servlet.init(config);
        }
    }
    
    public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
        assert req instanceof HttpServletRequest;
        String path = ((HttpServletRequest) req).getPathInfo();
        Servlet servlet = servlets.get(path);
        if (servlet == null) {
            int i;
            servlet = servlets.get(path + "/*");
            if (servlet == null) {
                while ((i = path.lastIndexOf("/")) >= 0) {
                    servlet = servlets.get(path.substring(0, i) + "/*");
                    if (servlet != null) {
                        break;
                    }
                    path = path.substring(0, i);
                }
            }
            if (servlet == null) {
                throw new IllegalStateException("No servlet registered for path: " + path);
            }
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