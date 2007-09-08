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

import org.fabric3.host.runtime.RuntimeLifecycleCoordinator;
import org.fabric3.host.runtime.Bootstrapper;
import org.fabric3.host.runtime.ScdlBootstrapper;

/**
 * @version $Rev$ $Date$
 */
public interface WebappUtil {

    String getApplicationName();

    WebappRuntime getRuntime(ClassLoader bootClassLoader) throws Fabric3InitException;

    /**
     * Return the classloader that should be used to boot the Fabric3 runtime. This will be a child of the web
     * application's ClassLoader.
     *
     * @param webappClassLoader the web application's classloader
     * @return a classloader that can be used to load the Fabric3 runtime classes
     */
    ClassLoader getBootClassLoader(ClassLoader webappClassLoader) throws InvalidResourcePath;

    ScdlBootstrapper getBootstrapper(ClassLoader bootClassLoader) throws Fabric3InitException;

    RuntimeLifecycleCoordinator<WebappRuntime, Bootstrapper> getCoordinator(ClassLoader bootClassLoader)
            throws Fabric3InitException;

    URL getSystemScdl(ClassLoader bootClassLoader) throws InvalidResourcePath;

    URL getApplicationScdl(ClassLoader bootClassLoader) throws InvalidResourcePath;

    URL getScdlURL(String path, ClassLoader classLoader) throws MalformedURLException;

    /**
     * Return a init parameter from the servlet context or provide a default.
     *
     * @param name  the name of the parameter
     * @param value the default value
     * @return the value of the specified parameter, or the default if not defined
     */
    String getInitParameter(String name, String value);
}
