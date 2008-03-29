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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;

/**
 * Notifies the Fabric3 runtime of session creation and expiration events.
 * 
 * @version $Rev$ $Date$
 */
public class Fabric3RequestListener implements ServletRequestListener {
    public void requestInitialized(ServletRequestEvent servletRequestEvent) {
        ServletContext context = servletRequestEvent.getServletContext();
        WebappRuntime runtime = (WebappRuntime) context.getAttribute(RUNTIME_ATTRIBUTE);
        if (runtime != null) {
            runtime.requestInitialized(servletRequestEvent);
        } else {
            context.log("requestInitialized", new ServletException("Fabric3 runtime not configured"));
        }
    }

    public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
        ServletContext context = servletRequestEvent.getServletContext();
        WebappRuntime runtime = (WebappRuntime) context.getAttribute(RUNTIME_ATTRIBUTE);
        if (runtime != null) {
            runtime.requestDestroyed(servletRequestEvent);
        }
    }
}