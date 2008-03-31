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
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;

/**
 * Maps incoming requests to a web application context to a servlet provided by a binding.
 *
 * @version $Revision$ $Date$
 */
public class Fabric3Servlet extends HttpServlet {
    private static final long serialVersionUID = 1548054328338375218L;
    private ServletRequestInjector requestInjector;

    public void init(ServletConfig config) throws ServletException {
        ServletContext servletContext = config.getServletContext();
        WebappRuntime runtime = (WebappRuntime) servletContext.getAttribute(Constants.RUNTIME_ATTRIBUTE);
        if (runtime == null) {
            throw new ServletException("Fabric3 runtime not configured for web application");
        }
        requestInjector = runtime.getRequestInjector();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        requestInjector.service(req, res);
    }
}