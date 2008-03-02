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
package org.fabric3.maven;

import java.beans.XMLDecoder;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.util.IOUtil;

/**
 * Helper class for resolving dependencies from WAR files.
 *
 * @author Administrator
 */
public class WarRepositoryHelper {

    /**
     * WAR Repository URL
     */
    private URL repositoryUrl;

    /**
     * Dependency metadata
     */
    private Map<String, Set<String>> transDependencyMap = new HashMap<String, Set<String>>();

    /**
     * Initializes the repository URL.
     *
     * @param baseUrl Base URL.
     */
    @SuppressWarnings("unchecked")
    public WarRepositoryHelper(URL baseUrl) {


        InputStream transDepMapInputStream = null;
        try {

            repositoryUrl = new URL(baseUrl, "repository/");
            URL transDependencyMapUrl = new URL(repositoryUrl, "dependency.metadata");
            transDepMapInputStream = transDependencyMapUrl.openStream();

            XMLDecoder decoder = new XMLDecoder(transDepMapInputStream);
            transDependencyMap = (Map<String, Set<String>>) decoder.readObject();
            decoder.close();

        } catch (MalformedURLException ex) {
            // throw new Fabric3DependencyException(ex);
        } catch (IOException ex) {
            // throw new Fabric3DependencyException(ex);
        } finally {
            IOUtil.close(transDepMapInputStream);
        }

    }

    /**
     * Resolves the dependencies transitively.
     *
     * @param rootArtifact Artifact whose dependencies need to be resolved.
     * @return true if the artifact was successfully resolved
     * @throws Fabric3DependencyException If unable to resolve the dependencies.
     */
    public boolean resolveTransitively(Artifact rootArtifact) throws Fabric3DependencyException {

        String artKey = rootArtifact.getGroup() + "/" + rootArtifact.getName() + "/" + rootArtifact.getVersion() + "/";
        if (!transDependencyMap.containsKey(artKey)) {
            return false;
        }


        for (String dep : transDependencyMap.get(artKey)) {

            String[] tokens = dep.split("/");
            String artName = tokens[1];

            try {
                if (artName.equals(rootArtifact.getName())) {
                    rootArtifact.setUrl(new URL(repositoryUrl, dep));
                } else {
                    Artifact depArtifact = new Artifact();
                    depArtifact.setGroup(tokens[0]);
                    depArtifact.setName(tokens[1]);
                    depArtifact.setVersion(tokens[2]);
                    depArtifact.setUrl(new URL(repositoryUrl, dep));
                    rootArtifact.addDependency(depArtifact);

                }
            } catch (MalformedURLException ex) {
                throw new Fabric3DependencyException(ex);
            }
        }
        return rootArtifact.getUrl() != null;
    }

}
