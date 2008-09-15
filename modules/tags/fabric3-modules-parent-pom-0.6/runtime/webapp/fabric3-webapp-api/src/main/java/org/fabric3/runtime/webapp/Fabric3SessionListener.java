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

import javax.servlet.http.HttpSessionListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.ServletContext;

import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;

/**
 * Notifies the WebappRuntime of session events.
 *
 * @version $Revision$ $Date$
 */
public class Fabric3SessionListener implements HttpSessionListener {
    private WebappRuntime runtime;

    public void sessionCreated(HttpSessionEvent event) {
        getRuntime(event.getSession().getServletContext()).sessionCreated(event);
    }

    public void sessionDestroyed(HttpSessionEvent event) {
        getRuntime(event.getSession().getServletContext()).sessionDestroyed(event);
    }

    private WebappRuntime getRuntime(ServletContext context) {
        if (runtime == null) {
            runtime = (WebappRuntime) context.getAttribute(RUNTIME_ATTRIBUTE);
        }
        return runtime;
    }

}
