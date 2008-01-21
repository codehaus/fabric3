/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.binding.ws.axis2.servlet;

import java.util.Enumeration;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

/**
 * @version $Revision$ $Date$
 */
public class F3Axis2ServletConfig implements ServletConfig {
    
    private ServletContext servletContext;
    
    /**
     * Initializes the servlet context.
     * 
     * @param servletContext Servlet context.
     */
    public F3Axis2ServletConfig(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameter(java.lang.String)
     */
    public String getInitParameter(String name) {
        return null;
    }

    /**
     * @see javax.servlet.ServletConfig#getInitParameterNames()
     */
    @SuppressWarnings("unchecked")
    public Enumeration getInitParameterNames() {
        return null;
    }

    /**
     * @see javax.servlet.ServletConfig#getServletContext()
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * @see javax.servlet.ServletConfig#getServletName()
     */
    public String getServletName() {
        return "Fabric3 Axis2 Servlet";
    }

}
