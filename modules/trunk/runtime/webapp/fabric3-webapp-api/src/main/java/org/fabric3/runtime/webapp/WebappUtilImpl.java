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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.logging.Level;
import javax.servlet.ServletContext;

import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.host.work.WorkScheduler;
import org.fabric3.jmx.agent.Agent;
import org.fabric3.jmx.agent.DefaultAgent;
import org.fabric3.jsr237.ThreadPoolWorkScheduler;
import org.fabric3.monitor.MonitorFactory;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.APPLICATION_SCDL_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.BOOTSTRAP_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.BOOTSTRAP_PARAM;
import static org.fabric3.runtime.webapp.Constants.COORDINATOR_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.COORDINATOR_PARAM;
import static org.fabric3.runtime.webapp.Constants.INTENTS_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.INTENTS_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.LOG_FORMATTER_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.LOG_FORMATTER_PARAM;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_PARAM;
import static org.fabric3.runtime.webapp.Constants.NUM_WORKERS_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.NUM_WORKERS_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_PARAM;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_MONITORING_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_MONITORING_PARAM;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_SCDL_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.SYSTEM_SCDL_PATH_PARAM;

/**
 * @version $Rev$ $Date$
 */
public class WebappUtilImpl implements WebappUtil {

    private static final String SYSTEM_CONFIG = "/WEB-INF/systemConfig.xml";

    private final ServletContext servletContext;

    public WebappUtilImpl(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public WebappRuntime getRuntime(ClassLoader bootClassLoader) throws Fabric3InitException {

        try {

            String monitorFactoryClass = getInitParameter(MONITOR_FACTORY_PARAM, MONITOR_FACTORY_DEFAULT);
            MonitorFactory factory = (MonitorFactory) bootClassLoader.loadClass(monitorFactoryClass).newInstance();

            String level = getInitParameter(SYSTEM_MONITORING_PARAM, SYSTEM_MONITORING_DEFAULT);
            factory.setDefaultLevel(Level.parse(level));
            factory.setBundleName("f3");
            String formatterClass = getInitParameter(LOG_FORMATTER_PARAM, LOG_FORMATTER_DEFAULT);
            Properties configuration = new Properties();
            configuration.setProperty("fabric3.jdkLogFormatter", formatterClass);
            factory.setConfiguration(configuration);

            // TODO Add better host JMX support from the next release
            Agent agent = new DefaultAgent();

            String className = getInitParameter(RUNTIME_PARAM, RUNTIME_DEFAULT);
            WebappRuntime runtime = (WebappRuntime) bootClassLoader.loadClass(className).newInstance();
            runtime.setMonitorFactory(factory);
            runtime.setMBeanServer(agent.getMBeanServer());

            String numWorkers = getInitParameter(NUM_WORKERS_PARAM, NUM_WORKERS_DEFAULT);
            WorkScheduler workScheduler = new ThreadPoolWorkScheduler(Integer.parseInt(numWorkers), false);
            runtime.setWorkScheduler(workScheduler);

            return runtime;

        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Runtime Implementation not found", e);
        }

    }

    public ScdlBootstrapper getBootstrapper(ClassLoader bootClassLoader) throws Fabric3InitException {

        try {

            String className = getInitParameter(BOOTSTRAP_PARAM, BOOTSTRAP_DEFAULT);
            ScdlBootstrapper scdlBootstrapper = (ScdlBootstrapper) bootClassLoader.loadClass(className).newInstance();
            scdlBootstrapper.setSystemConfig(servletContext.getResource(SYSTEM_CONFIG));

            return scdlBootstrapper;

        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Bootstrapper Implementation not found", e);
        } catch (MalformedURLException e) {
            throw new Fabric3InitException(e);
        }

    }

    @SuppressWarnings({"unchecked"})
    public RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper> getCoordinator(ClassLoader bootClassLoader) throws Fabric3InitException {

        try {

            String className = getInitParameter(COORDINATOR_PARAM, COORDINATOR_DEFAULT);
            return (RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper>) bootClassLoader.loadClass(className).newInstance();

        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Bootstrapper Implementation not found", e);
        }

    }

    public URL getSystemScdl(ClassLoader bootClassLoader) throws InvalidResourcePath {

        String path = getInitParameter(SYSTEM_SCDL_PATH_PARAM, SYSTEM_SCDL_PATH_DEFAULT);
        try {
            return convertToURL(path, bootClassLoader);
        } catch (MalformedURLException e) {
            throw new InvalidResourcePath(SYSTEM_SCDL_PATH_PARAM, path, e);
        }

    }

    public URL getIntentsLocation(ClassLoader bootClassLoader) throws InvalidResourcePath {

        String path = getInitParameter(INTENTS_PATH_PARAM, INTENTS_PATH_DEFAULT);
        try {
            return convertToURL(path, bootClassLoader);
        } catch (MalformedURLException e) {
            throw new InvalidResourcePath(SYSTEM_SCDL_PATH_PARAM, path, e);
        }

    }

    public String getApplicationName() {

        String name = servletContext.getServletContextName();
        if (name == null) {
            name = "application";
        }
        return name;

    }

    public URL getApplicationScdl(ClassLoader bootClassLoader) throws InvalidResourcePath {

        String path = getInitParameter(APPLICATION_SCDL_PATH_PARAM, APPLICATION_SCDL_PATH_DEFAULT);
        try {
            return convertToURL(path, bootClassLoader);
        } catch (MalformedURLException e) {
            throw new InvalidResourcePath(APPLICATION_SCDL_PATH_PARAM, path, e);
        }

    }

    public URL convertToURL(String path, ClassLoader classLoader) throws MalformedURLException {

        URL ret = null;
        if (path.charAt(0) == '/') {
            // user supplied an absolute path - look up as a webapp resource
            ret = servletContext.getResource(path);
        }
        if (ret == null) {
            // user supplied a relative path - look up as a boot classpath resource
            ret = classLoader.getResource(path);
        }
        return ret;

    }

    public String getInitParameter(String name, String value) {

        String result = servletContext.getInitParameter(name);
        if (result != null && result.length() != 0) {
            return result;
        }
        return value;

    }
}
