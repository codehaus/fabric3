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

import java.io.File;
import java.net.URI;
import java.net.URL;
import javax.servlet.ServletContext;

/**
 * @version $Rev$ $Date$
 */
public class WebappHostInfoImpl implements WebappHostInfo {
    private final ServletContext servletContext;
    private final URI domain;
    private final File baseDir;
    private final URL intentsLocation;
    private final boolean online;

    public WebappHostInfoImpl(ServletContext servletContext, URI domain, File baseDir, URL intentsLocation, boolean online) {
        this.servletContext = servletContext;
        this.domain = domain;
        this.baseDir = baseDir;
        this.intentsLocation = intentsLocation;
        this.online = online;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public URL getIntentsLocation() {
        return intentsLocation;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getInstallDirectory() {
        return new File("");
    }

    public boolean isOnline() {
        return online;
    }

    public String getProperty(String name, String defaultValue) {
        String val = servletContext.getInitParameter(name);
        return val == null ? defaultValue : val;
    }

    public URI getDomain() {
        return domain;
    }

}
