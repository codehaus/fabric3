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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.AxisServlet;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class F3AxisServlet extends AxisServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,
            IOException {
        // TODO Auto-generated method stub
        super.doPost(request, response);
    }

    private final ConfigurationContext configurationContext;
    
    /**
     * Initializes the Axis configuration context.
     * 
     * @param configurationContext Axis configuration context.
     */
    public F3AxisServlet(final ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }
    
    /**
     * Adds the Axis configuration context to the servlet context.
     * 
     * @see org.apache.axis2.transport.http.AxisServlet#init(javax.servlet.ServletConfig)
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        
        ServletContext servletContext = config.getServletContext();
        servletContext.setAttribute(AxisServlet.CONFIGURATION_CONTEXT, configurationContext);

        super.init(config);
        
    }

}
