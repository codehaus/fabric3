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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.fabric3.maven.runtime.MavenCoordinator;

/**
 *
 * @version $Revision$ $Date$
 */
public class ExtensionHelper {
    
    public ArtifactHelper artifactHelper;

    private List<URL> resolveDependencies(Dependency[] dependencies) throws MojoExecutionException {
        
        List<URL> urls = new ArrayList<URL>();
        
        if (dependencies == null) {
            return urls;
        }
        
        for (Dependency dependency : dependencies) {
            Artifact artifact = artifactHelper.resolve(dependency);
            try {
                urls.add(artifact.getFile().toURI().toURL());
            } catch (MalformedURLException e) {
                throw new AssertionError();
            }
        }
        
        return urls;
        
    }

    public void processExtensions(MavenCoordinator coordinator, Dependency[] extensions, Dependency[] userExtensions, 
            File[] userExtensionsArchives) throws MojoExecutionException, MalformedURLException {
        List<URL> extensionUrls = resolveDependencies(extensions);
        coordinator.setExtensions(extensionUrls);
        List<URL> userExtensionUrls = resolveDependencies(userExtensions);
        // add extensions that are not Maven artifacts
        if (userExtensionsArchives != null) {
            for (File entry : userExtensionsArchives) {
                if (!entry.exists()) {
                    throw new MojoExecutionException("User extension does not exist: " + entry);
                }
                userExtensionUrls.add(entry.toURI().toURL());
            }
        }
        coordinator.setUserExtensions(userExtensionUrls);
    }

}
