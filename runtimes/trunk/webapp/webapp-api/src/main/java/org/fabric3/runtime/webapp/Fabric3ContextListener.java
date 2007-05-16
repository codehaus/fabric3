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
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.fabric3.host.Fabric3RuntimeException;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.runtime.ShutdownException;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPONENT_PARAM;
import static org.fabric3.runtime.webapp.Constants.COMPOSITE_PARAM;
import static org.fabric3.runtime.webapp.Constants.ONLINE_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_ATTRIBUTE;

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
 * Fabric3SessionListener}. The latter notifies the runtime of session creation and expiration events through a
 * "bridging" contract, {@link WebappRuntime}. The <code>web.xml</code> may also optionally be configured with entries
 * for {@link Fabric3Filter} and {@link Fabric3Servlet}. The former must be mapped to all urls that execute "unmanaged"
 * code which accesses the Fabric3 runtime though the SCA API, for example, JSPs and Servlets. The latter forwards
 * service requests into the runtime, by default requests sent to URLs relative to the context path beginning with
 * <code>/services</code>.
 *
 * @version $Rev$ $Date$
 */
public class Fabric3ContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent event) {
        ClassLoader webappClassLoader = Thread.currentThread().getContextClassLoader();
        ServletContext servletContext = event.getServletContext();
        WebappUtil utils = getUtils(servletContext);
        try {
            // FIXME work this out from the servlet context
            URI domain = URI.create("fabric3://./domain");
            String defaultComposite = "webapp";
            URI compositeId = new URI(utils.getInitParameter(COMPOSITE_PARAM, defaultComposite));
            URI componentId = new URI(utils.getInitParameter(COMPONENT_PARAM, "webapp"));
            String scdlPath = utils.getInitParameter(APPLICATION_SCDL_PATH_PARAM, APPLICATION_SCDL_PATH_DEFAULT);
            URL scdl = servletContext.getResource(scdlPath);

            boolean online = Boolean.valueOf(utils.getInitParameter(ONLINE_PARAM, "true"));
            WebappHostInfo info = new WebappHostInfoImpl(servletContext,
                                                         domain,
                                                         servletContext.getResource("/WEB-INF/fabric3/"),
                                                         online);
            ClassLoader bootClassLoader = utils.getBootClassLoader(webappClassLoader);
            URL systemScdl = utils.getSystemScdl(bootClassLoader);

            WebappRuntime runtime = utils.getRuntime(bootClassLoader);
            runtime.setServletContext(servletContext);
            runtime.setHostInfo(info);
            runtime.setHostClassLoader(webappClassLoader);
            ScdlBootstrapper bootstrapper = utils.getBootstrapper(bootClassLoader);
            bootstrapper.setScdlLocation(systemScdl);
            bootstrapper.bootstrap(runtime, bootClassLoader);
            servletContext.setAttribute(RUNTIME_ATTRIBUTE, runtime);

            runtime.deploy(compositeId, scdl, componentId);
        } catch (Fabric3RuntimeException e) {
            servletContext.log(e.getMessage(), e);
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            servletContext.log(e.getMessage(), e);
            e.printStackTrace();
            throw new Fabric3InitException(e);
        }
    }

    protected WebappUtil getUtils(ServletContext servletContext) {
        return new WebappUtilImpl(servletContext);
    }

    public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        WebappRuntime runtime = (WebappRuntime) servletContext.getAttribute(RUNTIME_ATTRIBUTE);
        if (runtime == null) {
            return;
        }
        servletContext.removeAttribute(RUNTIME_ATTRIBUTE);
        try {
            runtime.destroy();
        } catch (ShutdownException e) {
            servletContext.log("Error destroying runtume", e);
        }
    }

}
