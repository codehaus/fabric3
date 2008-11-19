/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.management.MBeanServer;
import javax.servlet.ServletContext;

import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.jmx.agent.DefaultAgent;
import static org.fabric3.runtime.webapp.Constants.BOOTSTRAP_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.BOOTSTRAP_PARAM;
import static org.fabric3.runtime.webapp.Constants.COORDINATOR_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.COORDINATOR_PARAM;
import static org.fabric3.runtime.webapp.Constants.INTENTS_PATH_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.INTENTS_PATH_PARAM;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.MONITOR_FACTORY_PARAM;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_DEFAULT;
import static org.fabric3.runtime.webapp.Constants.RUNTIME_PARAM;
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

        WebappRuntime runtime = createRuntime(bootClassLoader);

        MonitorFactory factory = createMonitorFactory(bootClassLoader);
        MBeanServer mBeanServer = createMBeanServer();

        runtime.setMonitorFactory(factory);
        runtime.setMBeanServer(mBeanServer);

        return runtime;

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

    public String getInitParameter(String name, String value) {

        String result = servletContext.getInitParameter(name);
        if (result != null && result.length() != 0) {
            return result;
        }
        return value;

    }

    /**
     * Extension point for creating the MBean server.
     *
     * @return MBean server.
     * @throws Fabric3InitException If unable to initialize the MBean server.
     */
    private MBeanServer createMBeanServer() throws Fabric3InitException {
        return new DefaultAgent().getMBeanServer();
    }

    /**
     * Extension point for creating the runtime.
     *
     * @param bootClassLoader Classloader for loading the runtime class.
     * @return Webapp runtime instance.
     * @throws Fabric3InitException If unable to initialize the runtime.
     */
    private WebappRuntime createRuntime(ClassLoader bootClassLoader) throws Fabric3InitException {

        try {
            String className = getInitParameter(RUNTIME_PARAM, RUNTIME_DEFAULT);
            return (WebappRuntime) bootClassLoader.loadClass(className).newInstance();
        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Runtime Implementation not found", e);
        }

    }

    /**
     * Extension point for creating the monitor factory.
     *
     * @param bootClassLoader Classloader for loading the monitor factory class.
     * @return Monitor factory instance.
     * @throws Fabric3InitException If unable to initialize the monitor factory.
     */
    private MonitorFactory createMonitorFactory(ClassLoader bootClassLoader) throws Fabric3InitException {

        try {
            String monitorFactoryClass = getInitParameter(MONITOR_FACTORY_PARAM, MONITOR_FACTORY_DEFAULT);
            MonitorFactory factory = (MonitorFactory) bootClassLoader.loadClass(monitorFactoryClass).newInstance();
            URL configUrl = convertToURL(Constants.MONITOR_CONFIG_PATH, bootClassLoader);
            if (configUrl != null) {
                factory.readConfiguration(configUrl);
            }
            return factory;

        } catch (InstantiationException e) {
            throw new Fabric3InitException(e);
        } catch (IllegalAccessException e) {
            throw new Fabric3InitException(e);
        } catch (ClassNotFoundException e) {
            throw new Fabric3InitException("Monitor factory Implementation not found", e);
        } catch (IOException e) {
            throw new Fabric3InitException(e);
        }

    }

    URL convertToURL(String path, ClassLoader classLoader) throws MalformedURLException {

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

}
