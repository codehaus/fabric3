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

import org.fabric3.spi.services.artifact.ArtifactRepository;

/**
 * @version $Rev$ $Date$
 */
public class MavenHostInfoImpl implements MavenHostInfo {
    private final URI domain;
    private final ArtifactRepository artifactRepository;


    public MavenHostInfoImpl(URI domain, ArtifactRepository artifactRepository) {
        this.domain = domain;
        this.artifactRepository = artifactRepository;
    }

    public ArtifactRepository getArtifactRepository() {
        return artifactRepository;
    }

    public URL getBaseURL() {
        return null;
    }

    public boolean isOnline() {
        throw new UnsupportedOperationException();
    }

    public String getProperty(String name, String defaultValue) {
        return null;
    }

    public URI getDomain() {
        return domain;
    }

    public String getRuntimeId() {
        return "maven";
    }
}
