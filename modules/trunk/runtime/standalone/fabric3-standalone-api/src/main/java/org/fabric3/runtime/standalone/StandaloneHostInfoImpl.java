/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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

import org.fabric3.host.RuntimeMode;

/**
 * @version $Rev$ $Date$
 */
public class StandaloneHostInfoImpl implements StandaloneHostInfo {
    private final RuntimeMode runtimeMode;
    private final URI domain;
    private final File baseDir;
    private File modeConfigDirectory;
    private final Properties properties;
    private final File repositoryDirectory;
    private final File configDirectory;
    private final File tempDirectory;
    private File dataDirectory;

    /**
     * Constructor.
     *
     * @param runtimeMode         the mode the runtime is started in
     * @param domain              the SCA domain this runtime belongs to
     * @param baseDir             directory containing the standalone installation
     * @param repositoryDirectory directory containing the standalone repository
     * @param configDir           directory containing the standalone configuration
     * @param modeConfigDir       directory containing the standalone boot mode configuration
     * @param properties          properties for this runtime
     * @param tempDirectory       the directory for writing temporary files
     * @param dataDirectory       the directory for writing persistent data that survives restarts
     */
    public StandaloneHostInfoImpl(RuntimeMode runtimeMode,
                                  URI domain,
                                  File baseDir,
                                  File repositoryDirectory,
                                  File configDir,
                                  File modeConfigDir,
                                  Properties properties,
                                  File tempDirectory,
                                  File dataDirectory) {
        this.runtimeMode = runtimeMode;
        this.domain = domain;
        this.baseDir = baseDir;
        this.repositoryDirectory = repositoryDirectory;
        this.configDirectory = configDir;
        this.modeConfigDirectory = modeConfigDir;
        this.properties = properties;
        this.tempDirectory = tempDirectory;
        this.dataDirectory = dataDirectory;
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

    public File getDataDir() {
        return dataDirectory;
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public boolean supportsClassLoaderIsolation() {
        return true;
    }

    public File getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public File getConfigDirectory() {
        return configDirectory;
    }

    public File getModeConfigDirectory() {
        return modeConfigDirectory;
    }
}
