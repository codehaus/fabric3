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
package org.fabric3.runtime.standalone;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.fabric3.host.runtime.AbstractHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneHostInfoImpl extends AbstractHostInfo implements StandaloneHostInfo {
    private final File extensionsDirectory;
    private File configDirectory;
    private final ClassLoader hostClassLoader;
    private final ClassLoader bootClassLoader;

    /**
     * Initializes the base URL, install directory, application root directory and online mode.
     *
     * @param domain          the SCA domain this runtime belongs to
     * @param baseDir         directory containing the standalone installation
     * @param extensionsDir   directory containing the standalone extensions
     * @param configDir       directory containing the standalone configuration
     * @param online          true if this runtime should consider itself online
     * @param properties      properties for this runtime
     * @param hostClassLoader the runtime host's classloader
     * @param bootClassLoader the runtime bootstrap classloader
     */
    public StandaloneHostInfoImpl(final URI domain,
                                  final File baseDir,
                                  final File extensionsDir,
                                  final File configDir,
                                  final boolean online,
                                  final Properties properties,
                                  final ClassLoader hostClassLoader,
                                  final ClassLoader bootClassLoader) {
        super(domain, baseDir, online, properties);
        this.extensionsDirectory = extensionsDir;
        this.configDirectory = configDir;
        this.hostClassLoader = hostClassLoader;
        this.bootClassLoader = bootClassLoader;

    }

    public File getExtensionsDirectory() {
        return extensionsDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }
}
