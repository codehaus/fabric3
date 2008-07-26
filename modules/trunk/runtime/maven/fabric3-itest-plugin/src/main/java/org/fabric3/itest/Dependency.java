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

import java.util.List;

/**
 * Local subclass of Maven's dependency as Plexus looks for elements in the same package.
 *
 * @version $Rev$ $Date$
 */
public class Dependency extends org.apache.maven.model.Dependency {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2603000897594439278L;
    
    /**
     * No-argument constructor.
     */
    public Dependency() {
    }
    
    /**
     * Sets the group id, artifact id, version and exclusions.
     * 
     * @param groupId Group Id.
     * @param artifactId ARtifact Id.
     * @param version Version.
     * @param exclusions List of exclusions.
     */
    public Dependency(String groupId, String artifactId, String version, List<Exclusion> exclusions) {
        setGroupId(groupId);
        setVersion(version);
        setArtifactId(artifactId);
        setExclusions(exclusions);
    }

    /**
     * Implements equals based onartifactId, groupId and version.
     */
    @Override
    public boolean equals(Object obj) {
        
        if (!(obj instanceof Dependency)) {
            return false;
        }
        
        Dependency other = (Dependency) obj;
        return getArtifactId().equals(other.getArtifactId()) && 
               getGroupId().equalsIgnoreCase(other.getGroupId()) && 
               getVersion().equals(other.getVersion());
        
    }

    /**
     * Implements hashCode based onartifactId, groupId and version.
     */
    @Override
    public int hashCode() {
        
        int hash = 7;
        hash += 31 * getArtifactId().hashCode();
        hash += 31 * getGroupId().hashCode();
        hash += 31 * getVersion().hashCode();
        
        return hash;
        
    }
    
}
