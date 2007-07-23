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
    private final String profileName;
    private final File profileDirectory;
    private final File installDirectory;
    private final Properties properties;
    private final ClassLoader hostClassLoader;
    private final ClassLoader bootClassLoader;

    /**
     * Initializes the base URL, install directory, application root directory and online mode.
     *
     * @param domain                   the SCA domain this runtime belongs to
     * @param profileName              the runtime's profile name
     * @param installDirectory         directory containing the standalone installation
     * @param profileDirectory         directory containing this runtime's profile
     * @param online                   true if this runtime should consider itself online
     * @param properties               properties for this runtime
     * @param hostClassLoader          the runtime host's classloader
     * @param bootClassLoader          the runtime bootstrap classloader
     */
    public StandaloneHostInfoImpl(final URI domain,
                                  final String profileName,
                                  final File installDirectory,
                                  final File profileDirectory,
                                  final boolean online,
                                  final Properties properties,
                                  final ClassLoader hostClassLoader,
                                  final ClassLoader bootClassLoader) {
        super(domain, BootstrapHelper.toURL(installDirectory), online, profileName);
        this.profileName = profileName;
        this.profileDirectory = profileDirectory;
        this.properties = properties;
        this.installDirectory = installDirectory;
        this.hostClassLoader = hostClassLoader;
        this.bootClassLoader = bootClassLoader;

    }

    public String getProfileName() {
        return profileName;
    }

    public File getProfileDirectory() {
        return profileDirectory;
    }

    public File getInstallDirectory() {
        return installDirectory;
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public ClassLoader getHostClassLoader() {
        return hostClassLoader;
    }

    public ClassLoader getBootClassLoader() {
        return bootClassLoader;
    }
}
