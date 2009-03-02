/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.ws.axis2.runtime.servlet;

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
