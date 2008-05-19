package org.fabric3.jetty.plugin.impl;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Clean up Mojo for Fabric3 Jetty Plugin
 * @goal clean
 * @phase test-compile
 * @description Runs jetty6 directly from a maven project
 */
public class Fabric3JettyCleanMojo extends AbstractMojo {

	/**
	 * Fabric3 path.
	 */
	private static final String FABRIC3_PATH = "WEB-INF/fabric3";

    /**
     * Root directory for all html/jsp etc files
     *
     * @parameter expression="${basedir}/src/main/webapp"
     * @required
     */
    private File webAppSourceDirectory;

	/*
	 * @see org.mortbay.jetty.plugin.Jetty6RunMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		File bootDir = new File(webAppSourceDirectory, FABRIC3_PATH);
		deleteFile(bootDir);
	}

	/**
	 * Deletes the file created in fabric3-jetty:run
	 *
	 * @param file
	 */
	private void deleteFile(File file) {
		if(file.isDirectory()){
			for(File internalFile : file.listFiles()){
				deleteFile(internalFile);
			}
		}
		file.delete();
	}

}
