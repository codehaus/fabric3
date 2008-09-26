/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.war;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;

/**
 * Represents a configured Fabric3 dependency for boot and extension libraries.
 *
 * @version $Rev$ $Date$
 */
public class Dependency {

    private String groupId;
    private String artifactId;
    private String version;
    private String type = "jar";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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
    public Dependency(String groupId, String artifactId, String version) {
        super();
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * Gets the artifact using the specified artifact factory.
     *
     * @param artifactFactory Artifact factory to use.
     * @return Artifact identified by the dependency.
     */
    public Artifact getArtifact(ArtifactFactory artifactFactory) {
        return artifactFactory.createArtifact(groupId, artifactId, version, Artifact.SCOPE_RUNTIME, type);
    }

    /**
     * Checks whether the specified artifact has the same artifact id.
     *
     * @param artifact Artifact to be matched.
     * @return True if the specified artifact has the same id.
     */
    public boolean match(Artifact artifact) {
        return artifact.getArtifactId().equals(artifactId);
    }

    /**
     * Returns the version of dependency.
     *
     * @return the version of dependency
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Sets the version for dependency.
     *
     * @param version the version for dependency
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the artifact id.
     *
     * @return the artifact id
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * Returns the group id.
     *
     * @return the group id
     */
    public String getGroupId() {
        return groupId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dependency that = (Dependency) o;

        if (artifactId != null ? !artifactId.equals(that.artifactId) : that.artifactId != null) return false;
        if (groupId != null ? !groupId.equals(that.groupId) : that.groupId != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
