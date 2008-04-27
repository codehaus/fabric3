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
package org.fabric3.itest;

import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.Set;

import org.fabric3.maven.runtime.MavenHostInfo;

/**
 * @version $Rev$ $Date$
 */
public class MavenHostInfoImpl implements MavenHostInfo {
    private final URI domain;
    private final Properties hostProperties;
    private final Set<URL> dependencyUrls;

    public MavenHostInfoImpl(URI domain, Properties hostProperties, Set<URL> dependencyUrls) {
        this.domain = domain;
        this.hostProperties = hostProperties;
        this.dependencyUrls = dependencyUrls;
    }

    public URL getBaseURL() {
        return null;
    }

    public boolean isOnline() {
        throw new UnsupportedOperationException();
    }

    public String getProperty(String name, String defaultValue) {
        return hostProperties.getProperty(name, defaultValue);
    }

    public URI getDomain() {
        return domain;
    }

    public Set<URL> getDependencyUrls() {
        return dependencyUrls;
    }
}
