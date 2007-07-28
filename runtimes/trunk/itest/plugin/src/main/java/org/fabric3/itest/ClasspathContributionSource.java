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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionSource;

/**
 * @version $Revision$ $Date$
 */
public class ClasspathContributionSource implements ContributionSource {
    
    // Resource name
    private String resource;
    private ClassLoader cl;
    
    public ClasspathContributionSource(String resource, ClassLoader cl) {
        this.resource = resource;
        this.cl = cl;
    }

    public byte[] getChecksum() {
        return new byte[0];
    }

    public URL getLocation() {
        return cl.getResource(resource);
    }

    public InputStream getSource() throws IOException {
        return cl.getResourceAsStream(resource);
    }

    public long getTimestamp() {
        return System.currentTimeMillis();
    }

    public URI getUri() {
        try {
            return getLocation().toURI();
        } catch(Exception ex) {
            throw new AssertionError(ex);
        }
    }

    public boolean isLocal() {
        return true;
    }

}
