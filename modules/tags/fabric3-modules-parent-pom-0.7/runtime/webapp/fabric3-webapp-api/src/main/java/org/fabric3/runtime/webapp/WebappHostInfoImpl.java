/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
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

import java.io.File;
import java.net.URI;
import javax.servlet.ServletContext;

/**
 * @version $Rev$ $Date$
 */
public class WebappHostInfoImpl implements WebappHostInfo {

    private final ServletContext servletContext;
    private final URI domain;
    private final File baseDir;
    private File tempDirectory;

    public WebappHostInfoImpl(ServletContext servletContext, URI domain, File baseDir, File tempDirectory) {
        this.servletContext = servletContext;
        this.domain = domain;
        this.baseDir = baseDir;
        this.tempDirectory = tempDirectory;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public File getBaseDir() {
        return baseDir;
    }

    public File getTempDir() {
        return tempDirectory;
    }

    public File getInstallDirectory() {
        throw new UnsupportedOperationException();
    }

    public String getProperty(String name, String defaultValue) {
        String val = servletContext.getInitParameter(name);
        return val == null ? defaultValue : val;
    }

    public URI getDomain() {
        return domain;
    }

    public boolean supportsClassLoaderIsolation() {
        return false;
    }
}
