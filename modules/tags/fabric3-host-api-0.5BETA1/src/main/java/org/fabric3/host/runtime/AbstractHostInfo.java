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
package org.fabric3.host.runtime;

import java.io.File;
import java.net.URI;
import java.util.Properties;

/**
 * Abstract host info implementation.
 *
 * @version $Revision$ $Date$
 */
public abstract class AbstractHostInfo implements HostInfo {

    /**
     * This SCA Domain this runtime belongs to.
     */
    private final URI domain;

    /**
     * Base URL.
     */
    private final File baseDir;

    /**
     * Online indicator.
     */
    private final boolean online;

    private Properties properties;

    /**
     * Initializes the runtime info instance.
     *
     * @param domain     the SCA Domain that this runtime belongs to.
     * @param baseDir    the base runtime directory.
     * @param online     Onlne indicator.
     * @param properties the runtime properties
     */
    public AbstractHostInfo(final URI domain, final File baseDir, final boolean online, final Properties properties) {
        this.domain = domain;
        this.baseDir = baseDir;
        this.online = online;
        this.properties = properties;
    }

    public URI getDomain() {
        return domain;
    }

    public final File getBaseDir() {
        return baseDir;
    }

    public final boolean isOnline() {
        return online;
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

}
