package org.fabric3.contribution.stub;

import java.io.File;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.stubs.ArtifactStub;
import org.apache.maven.project.MavenProject;

/*
 * Represents a maven artifact that is referenced or generated during the build
 */
public class SCAArtifactStub extends ArtifactStub {

	public SCAArtifactStub() {
		super();
	}

	public String getVersion() {
		if (super.getVersion() == null) {
			super.setVersion("0.0-Test");
		}
		return super.getVersion();
	}

	public String getArtifactId() {
		if (super.getArtifactId() == null) {
			super.setArtifactId("sca-contribution-plugin-test");
		}
		return super.getArtifactId();
	}

	public String getGroupId() {
		if (super.getGroupId() == null) {
			super.setGroupId("org.codehaus.fabric3");
		}
		return super.getGroupId();
	}

	public String getClassifier() {
		return super.getClassifier();
	}

	public String getScope() {
		if (super.getScope() == null) {
			super.setScope(Artifact.SCOPE_RUNTIME);
		}
		return super.getScope();
	}

	public boolean isOptional() {
		return super.isOptional();
	}

	public String getType() {
		if (super.getType() == null) {
			super.setType("sca-contribution");
		}
		return super.getType();
	}

	public ArtifactHandler getArtifactHandler() {
		return new DefaultArtifactHandler(getType());
	}

	public VersionRange getVersionRange() {
		return VersionRange.createFromVersion(getVersion());
	}
}
