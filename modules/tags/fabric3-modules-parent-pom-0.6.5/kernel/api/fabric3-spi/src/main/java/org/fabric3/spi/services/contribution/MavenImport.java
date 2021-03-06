/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.spi.services.contribution;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;

/**
 * Represents an Maven export entry in a contribution manifest.
 *
 * @version $Rev$ $Date$
 */
public class MavenImport implements Import {
    private static final long serialVersionUID = -252985481705630453L;
    private static final QName TYPE = new QName(Constants.FABRIC3_MAVEN_NS, "maven");

    private String groupId;
    private String artifactId;
    private String version = "unspecified";
    private String classifier = "jar";
    private String majorVersion = "";
    private String minorVersion = "";
    private String revision = "";
    private boolean snapshot;
    private URI location;

    public URI getLocation() {
        return location;
    }

    public void setLocation(URI location) {
        this.location = location;
    }

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

    public String getMajorVersion() {
        return majorVersion;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public String getRevision() {
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
        majorVersion = "";
        minorVersion = "";
        revision = "";
        snapshot = false;
        String[] tokens = version.split("\\.|\\-");
        if (tokens.length < 2) {
            majorVersion = version;
        } else if (tokens.length == 2) {
            majorVersion = tokens[0];
            minorVersion = tokens[1];
        } else if (tokens.length == 3) {
            majorVersion = tokens[0];
            minorVersion = tokens[1];
            if (!tokens[2].equals("SNAPSHOT")) {
                revision = tokens[2];
            } else {
                snapshot = true;
            }
        } else if (tokens.length == 4) {
            majorVersion = tokens[0];
            minorVersion = tokens[1];
            revision = tokens[2];
            snapshot = true;
        } else {
            throw new IllegalArgumentException("Illegal Maven version number: " + version);
        }
    }

    public String toString() {
        return new StringBuilder().append("Maven [").append(groupId).append(":").append(artifactId).
                append(":").append(version).append(":").append(classifier).append("]").toString();
    }

}
