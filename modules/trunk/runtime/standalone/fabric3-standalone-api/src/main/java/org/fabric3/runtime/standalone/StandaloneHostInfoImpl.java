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
package org.fabric3.runtime.standalone;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.fabric3.host.runtime.RuntimeMode;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneHostInfoImpl implements StandaloneHostInfo {
    private final RuntimeMode runtimeMode;
    private final URI domain;
    private final File baseDir;
    private File modeConfigDirectory;
    private final Properties properties;
    private final File extensionsDirectory;
    private final File configDirectory;
    private final File tempDirectory;

    /**
     * Constructor.
     *
     * @param runtimeMode   the mode the runtime is started in
     * @param domain        the SCA domain this runtime belongs to
     * @param baseDir       directory containing the standalone installation
     * @param extensionsDir directory containing the standalone extensions
     * @param configDir     directory containing the standalone configuration
     * @param modeConfigDir directory containing the standalone boot mode configuration
     * @param properties    properties for this runtime
     * @param tempDirectory the directory for writing temporary files
     */
    public StandaloneHostInfoImpl(RuntimeMode runtimeMode,
                                  URI domain,
                                  File baseDir,
                                  File extensionsDir,
                                  File configDir,
                                  File modeConfigDir,
                                  Properties properties,
                                  File tempDirectory) {
        this.runtimeMode = runtimeMode;
        this.domain = domain;
        this.baseDir = baseDir;
        this.extensionsDirectory = extensionsDir;
        this.configDirectory = configDir;
        this.modeConfigDirectory = modeConfigDir;
        this.properties = properties;
        this.tempDirectory = tempDirectory;
    }

    public RuntimeMode getRuntimeMode() {
        return runtimeMode;
    }

    public URI getDomain() {
        return domain;
    }

    public final File getBaseDir() {
        return baseDir;
    }

    public File getTempDir() {
        return tempDirectory;
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public boolean supportsClassLoaderIsolation() {
        return true;
    }

    public File getExtensionsDirectory() {
        return extensionsDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public File getModeConfigDirectory() {
        return modeConfigDirectory;
    }
}
