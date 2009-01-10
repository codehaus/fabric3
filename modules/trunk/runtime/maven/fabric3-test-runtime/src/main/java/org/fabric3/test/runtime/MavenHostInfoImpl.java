/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
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
package org.fabric3.test.runtime;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.fabric3.host.RuntimeMode;
import org.fabric3.test.runtime.api.MavenHostInfo;

public class MavenHostInfoImpl implements MavenHostInfo {
    
    private final URI domainUri = URI.create("fabric3://domain");
    private final File tempDir;
    private final Properties hostProperties;
    
    /**
     * Initializes the domain uri and temp directory.
     * 
     * @param hostProperties Host properties.
     */
    public MavenHostInfoImpl(Properties hostProperties) {

        this.hostProperties = hostProperties;
        
        tempDir = new File(System.getProperty("java.io.tmpdir"), ".f3");
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }
        
    }

    /**
     * Null, doesn't support persistent contributions.
     */
    public File getBaseDir() {
        return null;
    }

    /**
     * Gets the URI of the domain.
     */
    public URI getDomain() {
        return domainUri;
    }

    /**
     * Gets the specified host property or if not present system property.
     */
    public String getProperty(String name, String defaultValue) {
        
        String value = null;
        if (hostProperties != null) {
            value = hostProperties.getProperty(name);
        }
        
        if (name == null) {
            value = System.getProperty(name, defaultValue);
        }
        
        return value;
        
    }

    /**
     * Returns <code>RuntimeMode.VM</code>
     */
    public RuntimeMode getRuntimeMode() {
        return RuntimeMode.VM;
    }

    /**
     * Returns the <code>f3</code> user's temporary directory.
     */
    public File getTempDir() {
        return tempDir;
    }

    /**
     * True to indicate that the runtime supports CL isolation.
     */
    public boolean supportsClassLoaderIsolation() {
        return true;
    }

}
