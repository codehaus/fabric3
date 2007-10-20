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

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * Represents a configured Fabric3 dependency for boot and extension libraries.
 *
 * @version $Rev$ $Date$
 */
public class Dependency {

    private static final String TYPE_JAR = "jar";

    private String groupId;
    private String artifactId;
    private String version;
    private Set<Exclusion> exclusions = new HashSet<Exclusion>();
    
    

    /**
     * Default constructor.
     */
    public Dependency() {
    }

    /**
     * Initializes the field.
     *
     * @param groupId    Group id.
     * @param artifactId Artifact id.
     * @param version    Artifact version.
     */
    public Dependency(String groupId, String artifactId, String version, Set<Exclusion> exclusions) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.exclusions = exclusions;
    }

    /**
     * Gets the artifact using the specified artifact factory.
     *
     * @param artifactFactory Artifact factory to use.
     * @return Artifact identified by the dependency.
     */
    public Artifact getArtifact(ArtifactFactory artifactFactory) {
        return artifactFactory.createArtifact(groupId, artifactId, version, Artifact.SCOPE_RUNTIME, TYPE_JAR);
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public Set<Exclusion> getExclusions() {
        return exclusions;
    }
    
}
