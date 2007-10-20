package org.fabric3.contribution.stub;

import java.util.HashSet;
import java.util.LinkedList;

import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
/*
 * This is a stub class for the maven project. Attempting to extend MavenProjectStub didn't
 * work. A real POM could be read in during each test but since we are testing only
 * the mojo it doesn't make sense. This class fills in the stub so the clone operations succeed.
 */
public class SCAMavenProjectStub extends MavenProject{
	
	public SCAMavenProjectStub(Model model){
		super(model);
        super.setDependencyArtifacts( new HashSet() );
        super.setArtifacts( new HashSet() );
        super.setPluginArtifacts( new HashSet() );
        super.setReportArtifacts( new HashSet() );
        super.setExtensionArtifacts( new HashSet() );
        super.setRemoteArtifactRepositories( new LinkedList() );
        super.setPluginArtifactRepositories( new LinkedList() );
        super.setCollectedProjects( new LinkedList() );
        super.setActiveProfiles( new LinkedList() );
        //super.setOriginalModel( model );
        super.setOriginalModel( null );
        super.setExecutionProject( this );
	}

}
