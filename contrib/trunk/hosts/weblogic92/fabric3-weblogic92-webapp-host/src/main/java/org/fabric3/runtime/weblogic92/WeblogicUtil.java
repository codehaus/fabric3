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
package org.fabric3.runtime.weblogic92;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletContext;

import org.fabric3.runtime.webapp.Fabric3InitException;
import org.fabric3.runtime.webapp.WebappUtilImpl;

/**
 * Wires the services provided by Weblogic.
 * 
 * TODO Wire in other facilities like work managers, txm etc.
 */
public class WeblogicUtil extends WebappUtilImpl {

    /**
     * @param servletContext
     */
    public WeblogicUtil(ServletContext servletContext) {
        super(servletContext);
    }
    
    /*
     * Gets the MBean server from Weblogic.
     */
    @Override
    protected MBeanServer createMBeanServer() throws Fabric3InitException {
        
        Context ctx = null;        
        try {
            ctx = new InitialContext();
            Object mbeanServer = ctx.lookup("java:comp/env/jmx/runtime");
            return (MBeanServer) PortableRemoteObject.narrow(mbeanServer, MBeanServer.class);
        } catch (NamingException e) {
            throw new Fabric3InitException(e);
		} finally {
            try {
				ctx.close();
			} catch (NamingException e) {
	            throw new Fabric3InitException(e);
			}
        }
        
    }

}
