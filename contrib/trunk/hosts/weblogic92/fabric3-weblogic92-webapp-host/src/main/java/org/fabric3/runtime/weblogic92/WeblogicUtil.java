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

import static org.fabric3.runtime.webapp.Constants.RUNTIME_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_PARAM;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_MONITORING_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_MONITORING_PARAM;

import java.util.logging.Level;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.servlet.ServletContext;

import org.fabric3.monitor.MonitorFactory;
import org.fabric3.monitor.impl.JavaLoggingMonitorFactory;
import org.fabric3.runtime.webapp.Fabric3InitException;
import org.fabric3.runtime.webapp.WebappRuntime;
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

    /**
     * 
     */
    @Override
    public WebappRuntime getRuntime(ClassLoader bootClassLoader) throws Fabric3InitException {
        
        try {
            
            String className = getInitParameter(RUNTIME_PARAM, RUNTIME_DEFAULT);
            
            MBeanServer mBeanServer = getMBeanServer();
            MonitorFactory monitorFactory = getMonitorFactory();
            
            WebappRuntime runtime = (WebappRuntime) bootClassLoader.loadClass(className).newInstance();
            
            runtime.setMonitorFactory(monitorFactory);
            runtime.setMBeanServer(mBeanServer);
            
            return runtime;
            
        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Runtime Implementation not found", e);
        } catch (NamingException e) {
            throw new Fabric3InitException(e);
        }
        
    }
    
    /*
     * Gets the MBean server from Weblogic.
     */
    private MBeanServer getMBeanServer() throws NamingException {
        
        Context ctx = null;        
        try {
            ctx = new InitialContext();
            Object mbeanServer = ctx.lookup("java:comp/env/jmx/runtime");
            return (MBeanServer) PortableRemoteObject.narrow(mbeanServer, MBeanServer.class);
        } finally {
            ctx.close();
        }
        
    }
    
    /*
     * Gets the monitor factory.
     * 
     * TODO Get the monitor factory using the WLS logger.
     */
    private MonitorFactory getMonitorFactory() {
        
        String level = getInitParameter(SYSTEM_MONITORING_PARAM, SYSTEM_MONITORING_DEFAULT);
        MonitorFactory factory = new JavaLoggingMonitorFactory();
        factory.setDefaultLevel(Level.parse(level));
        factory.setBundleName("f3");
        
        return factory;
        
    }

}
