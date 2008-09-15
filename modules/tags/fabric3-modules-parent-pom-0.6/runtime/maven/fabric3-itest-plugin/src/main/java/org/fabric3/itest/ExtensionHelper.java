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
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.fabric3.featureset.FeatureSet;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.maven.runtime.MavenEmbeddedRuntime;

/**
 * @version $Revision$ $Date$
 */
public class ExtensionHelper {

    public ArtifactHelper artifactHelper;

    public void processExtensions(BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration,
                                  Dependency[] extensions,
                                  List<FeatureSet> featureSets,
                                  Dependency[] userExtensions,
                                  File[] userExtensionsArchives) throws MojoExecutionException {
        List<URL> extensionUrls = resolveDependencies(extensions);

        if (featureSets != null) {
            for (FeatureSet featureSet : featureSets) {
                extensionUrls.addAll(processFeatures(featureSet));
            }
        }
        List<ContributionSource> sources = createContributionSources(extensionUrls);
        configuration.setExtensions(sources);

        List<URL> userExtensionUrls = resolveDependencies(userExtensions);
        // add extensions that are not Maven artifacts
        if (userExtensionsArchives != null) {
            for (File entry : userExtensionsArchives) {
                if (!entry.exists()) {
                    throw new MojoExecutionException("User extension does not exist: " + entry);
                }
                try {
                    userExtensionUrls.add(entry.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("Invalid user extension URL: " + entry, e);
                }
            }
        }
        sources = createContributionSources(userExtensionUrls);
        configuration.setUserExtensions(sources);
    }

    private List<ContributionSource> createContributionSources(List<URL> urls) {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        for (URL extensionUrl : urls) {
            // it's ok to assume archives are uniquely named since most server environments have a single deploy directory
            URI uri = URI.create(new File(extensionUrl.getFile()).getName());
            ContributionSource source = new FileContributionSource(uri, extensionUrl, -1, new byte[0]);
            sources.add(source);
        }
        return sources;
    }

    private List<URL> processFeatures(FeatureSet featureSet) throws MojoExecutionException {
        Set<Dependency> dependencies = featureSet.getExtensions();
        return resolveDependencies(featureSet.getExtensions().toArray(new Dependency[dependencies.size()]));
    }

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

}
