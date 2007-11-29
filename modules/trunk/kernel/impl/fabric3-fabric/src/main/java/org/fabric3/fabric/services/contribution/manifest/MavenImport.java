/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.services.contribution.manifest;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.services.contribution.Import;

/**
 * Represents an Maven export entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class MavenImport extends Import {
    private static final long serialVersionUID = -252985481705630453L;
    private static final QName TYPE = new QName(Constants.FABRIC3_MAVEN_NS, "maven");

    private String groupId;
    private String artifactId;
    private String version = "unspecified";
    private String classifier = "jar";
    private int majorVersion = -1;
    private int minorVersion = -1;
    private int revision = -1;
    private boolean snapshot;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
        parseVersion();
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public int getRevision() {
        return revision;
    }

    public boolean isSnapshotVersion() {
        return snapshot;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public QName getType() {
        return TYPE;
    }

    private void parseVersion() {
        majorVersion = -1;
        minorVersion = -1;
        revision = -1;
        snapshot = false;
        String[] tokens = version.split("\\.|\\-");
        if (tokens.length < 2) {
            majorVersion = Integer.parseInt(version);
        } else if (tokens.length == 2) {
            majorVersion = Integer.parseInt(tokens[0]);
            minorVersion = Integer.parseInt(tokens[1]);
        } else if (tokens.length == 3) {
            majorVersion = Integer.parseInt(tokens[0]);
            minorVersion = Integer.parseInt(tokens[1]);
            if (!tokens[2].equals("SNAPSHOT")) {
                revision = Integer.parseInt(tokens[2]);
            } else {
                snapshot = true;
            }
        } else if (tokens.length == 4) {
            majorVersion = Integer.parseInt(tokens[0]);
            minorVersion = Integer.parseInt(tokens[1]);
            revision = Integer.parseInt(tokens[2]);
            snapshot = true;
        } else {
            throw new IllegalArgumentException("Illegal Maven version number [" + version + "]");
        }
    }

    public String toString() {
        return new StringBuilder().append("Maven import [").append(groupId).append(":").append(artifactId).
                append(":").append(version).append(":").append(classifier).append("]").toString();
    }
    
}
