/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
package org.fabric3.test.artifact;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Helper class for resolving artifacts.
 */
public class ArtifactHelper {

    public ArtifactFactory artifactFactory;
    public ArtifactResolver resolver;
    public ArtifactMetadataSource metadataSource;

    private ArtifactRepository localRepository;
    private List<?> remoteRepositories;

    /**
     * Sets the repositories to use.
     *
     * @param localRepository    Local repository to use.
     * @param remoteRepositories Remote reporitories to use.
     */
    public void setRepositories(ArtifactRepository localRepository, List<?> remoteRepositories) {
        this.localRepository = localRepository;
        this.remoteRepositories = remoteRepositories;
    }

    /**
     * Resolves the requested artifact transitively.
     *
     * @param groupId    Group Id.
     * @param artifactId Artifact Id.
     * @param version    Version number.
     * @param scope      Scope of the artifact.
     * @param type       Type of the artifact.
     * @return Transitively resolved set of artifacts including the root.
     */
    public Set<URL> resolve(String groupId, String artifactId, String version, String scope, String type) throws MojoExecutionException {

        try {

            Set<URL> artifacts = new HashSet<URL>();

            Artifact artifact = artifactFactory.createArtifact(groupId, artifactId, version, scope, type);

            resolver.resolve(artifact, remoteRepositories, localRepository);
            artifacts.add(artifact.getFile().toURL());

            ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact, localRepository, remoteRepositories);
            ArtifactFilter filter = new ArtifactFilter() {
                public boolean include(Artifact artifact) {
                    return true;
                }
            };

            ArtifactResolutionResult result = resolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                                           artifact,
                                                                           Collections.emptyMap(),
                                                                           localRepository,
                                                                           remoteRepositories,
                                                                           metadataSource,
                                                                           filter);
            for (Object transitiveArtifact : result.getArtifacts()) {
                artifacts.add(Artifact.class.cast(transitiveArtifact).getFile().toURL());
            }

            return artifacts;

        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

}
