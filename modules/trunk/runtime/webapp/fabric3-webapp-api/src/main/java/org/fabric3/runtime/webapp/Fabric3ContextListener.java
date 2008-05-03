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

import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.namespace.QName;

import org.fabric3.api.annotation.LogLevel;
import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.rmi.RmiAgent;

import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPONENT_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_PARAM;
import static org.fabric3.runtime.webapp.Constants.DOMAIN_PARAM;
import static org.fabric3.runtime.webapp.Constants.MANAGEMENT_DOMAIN_PARAM;
import static org.fabric3.runtime.webapp.Constants.DEFAULT_MANAGEMENT_DOMAIN;
import static org.fabric3.runtime.webapp.Constants.ONLINE_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_NAMESPACE_PARAM;

/**
 * Launches a Fabric3 runtime in a web application, loading information from servlet context parameters. This listener
 * manages one runtime per servlet context; the lifecycle of that runtime corresponds to the the lifecycle of the
 * associated servlet context.
 * <p/>
 * The runtime is launched in a child classloader of the web application, thereby providing isolation between
 * application and system artifacts. Application code only has access to the SCA API and may not reference Fabric3
 * system artifacts directly.
 * <p/>
 * The <code>web.xml</code> of a web application embedding Fabric3 must have entries for this listener and {@link
 * Fabric3ContextListener}. The latter notifies the runtime of session creation and expiration events through a
 * "bridging" contract, {@link WebappRuntime}.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3ContextListener implements ServletContextListener {
    
    private RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper> coordinator;
    private Agent agent;

    public void contextInitialized(ServletContextEvent event) {
        
        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        ServletContext servletContext = event.getServletContext();
        WebappUtil utils = getUtils(servletContext);
        WebappRuntime runtime;
        WebAppMonitor monitor = null;
        
        try {
            
            // FIXME work this out from the servlet context
            URI domain = new URI(utils.getInitParameter(DOMAIN_PARAM, "fabric3://./domain"));
            String defaultComposite = "WebappComposite";
            String compositeNamespace = utils.getInitParameter(COMPOSITE_NAMESPACE_PARAM, null);
            String compositeName = utils.getInitParameter(COMPOSITE_PARAM, defaultComposite);
            URI componentId = new URI(utils.getInitParameter(COMPONENT_PARAM, "webapp"));
            String scdlPath = utils.getInitParameter(APPLICATION_SCDL_PATH_PARAM, APPLICATION_SCDL_PATH_DEFAULT);
            URL scdl = servletContext.getResource(scdlPath);
            if (scdl == null) {
                throw new InitializationException("Web composite not found");
            }

            boolean online = Boolean.valueOf(utils.getInitParameter(ONLINE_PARAM, "true"));
            ClassLoader bootClassLoader = utils.getBootClassLoader(webappClassLoader);
            URL intentsLocation = utils.getIntentsLocation(bootClassLoader);
            URL baseDir = servletContext.getResource("/WEB-INF/fabric3/");
            WebappHostInfo info = new WebappHostInfoImpl(servletContext,
                                                         domain,
                                                         baseDir,
                                                         intentsLocation,
                                                         online);
            URL systemScdl = utils.getSystemScdl(bootClassLoader);

            runtime = utils.getRuntime(bootClassLoader);
            runtime.setHostInfo(info);
            runtime.setHostClassLoader(webappClassLoader);
            monitor = runtime.getMonitorFactory().getMonitor(WebAppMonitor.class);
            String managementDomain = utils.getInitParameter(MANAGEMENT_DOMAIN_PARAM, DEFAULT_MANAGEMENT_DOMAIN);
            runtime.setJMXDomain(managementDomain);
            agent = RmiAgent.newInstance();
            agent.start();
            runtime.setMBeanServer(agent.getMBeanServer());
            
            // initiate the runtime bootstrap sequence
            ScdlBootstrapper bootstrapper = utils.getBootstrapper(bootClassLoader);
            bootstrapper.setScdlLocation(systemScdl);
            coordinator = utils.getCoordinator(bootClassLoader);
            coordinator.bootPrimordial(runtime, bootstrapper, bootClassLoader, webappClassLoader);
            coordinator.initialize();
            Future<Void> joinFuture = coordinator.joinDomain(-1);
            joinFuture.get();
            Future<Void> recoverFuture = coordinator.recover();
            recoverFuture.get();
            Future<Void> startFuture = coordinator.start();
            startFuture.get();
            servletContext.setAttribute(RUNTIME_ATTRIBUTE, runtime);
            
            // deploy the application composite
            QName qName = new QName(compositeNamespace, compositeName);
            runtime.activate(qName, componentId);
            
        } catch (Fabric3RuntimeException e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw e;
        } catch (Throwable e) {
            if (monitor != null) {
                monitor.runError(e);
            }
            throw new Fabric3InitException(e);
        }
    }

    protected WebappUtil getUtils(ServletContext servletContext) {
        return new WebappUtilImpl(servletContext);
    }

    public void contextDestroyed(ServletContextEvent event) {
        
        ServletContext servletContext = event.getServletContext();
        WebappRuntime runtime = (WebappRuntime) servletContext.getAttribute(RUNTIME_ATTRIBUTE);
        
        if (runtime != null) {
            servletContext.removeAttribute(RUNTIME_ATTRIBUTE);
        }
        
        try {
            
            if (agent != null) {
                agent.shutdown();
            }
            
            Future<Void> future = coordinator.shutdown();
            future.get();
            
        } catch (ShutdownException e) {
            servletContext.log("Error shutting runtume down", e);
        } catch (ExecutionException e) {
            servletContext.log("Error shutting runtume down", e);
        } catch (InterruptedException e) {
            servletContext.log("Error shutting runtume down", e);
        }
        
    }

    public interface WebAppMonitor {
        @LogLevel("SEVERE")
        void runError(Throwable e);
    }

}
