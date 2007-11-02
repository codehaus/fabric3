package org.fabric3.contribution.stub;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;

/*
 * Some Maven internal classes deep clone the MavenProject including the model so
 * it needs to be stubbed too.
 */
public class SCAModelStub extends Model {
	
	public SCAModelStub() {}

	public String getVersion() {
		return "0.0-TEST";
	}

	public String getModelVersion() {
		return "0.0-TEST";
	}

	public String getName() {
		return "Test Model";
	}

	public String getGroupId() {
		return "org.codehaus.fabric3";
	}

	public String getPackaging() {
		return "sca-contribution";
	}

	public Parent getParent() {
		return new Parent();
	}

	public String getArtifactId() {
		return "sca-contribution-plugin-test";
	}

	public Properties getProperties() {
		return new Properties();
	}

	public List getPackages() {
		return new LinkedList();
	}

	public List getProfiles() {
		return new LinkedList();
	}

	public List getModules() {
		return new LinkedList();
	}
	
}
