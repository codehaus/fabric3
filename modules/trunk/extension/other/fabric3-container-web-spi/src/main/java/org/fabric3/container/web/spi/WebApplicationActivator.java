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
package org.fabric3.container.web.spi;

import java.net.URL;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;

import org.osoa.sca.ComponentContext;

import org.fabric3.pojo.reflection.Injector;

/**
 * Responsible for activating a web application in an embedded servlet container.
 *
 * @version $Revision$ $Date$
 */
public interface WebApplicationActivator {
    public static final String SERVLET_CONTEXT_SITE = "fabric3.servletContext";
    public static final String SESSION_CONTEXT_SITE = "fabric3.sessionContext";
    public static final String CONTEXT_ATTRIBUTE = "fabric3.context";

    /**
     * Returns the classloader to use for the web component corresponding the given id
     *
     * @param componentId the web component id
     * @return the classloader
     */
    ClassLoader getWebComponentClassLoader(URI componentId);

    /**
     * Perform the activation, which will result in making the web application available for incoming requests to the runtime.
     *
     * @param contextPath         the context path the web application will be available at. The context path is relative to the absolute address of
     *                            the embedded servlet container.
     * @param url                 a URL pointing to the WAR containing the web application assets
     * @param parentClassLoaderId the id for parent classloader to use for the web application
     * @param injectors           the map of artifact ids to injectors. An artifact id identifies an artifact type such as a servlet class name or
     *                            ServletContext.
     * @param context             the component context for the web component
     * @return the servlet context associated with the activated web application
     * @throws WebApplicationActivationException
     *          if an error occurs activating the web application
     */
    ServletContext activate(String contextPath, URL url, URI parentClassLoaderId, Map<String, List<Injector<?>>> injectors, ComponentContext context)
            throws WebApplicationActivationException;

    /**
     * Removes an activated web application
     *
     * @param url the URL the web application was activated with
     * @throws WebApplicationActivationException
     *          if an error occurs activating the web application
     */
    void deactivate(URL url) throws WebApplicationActivationException;

}
