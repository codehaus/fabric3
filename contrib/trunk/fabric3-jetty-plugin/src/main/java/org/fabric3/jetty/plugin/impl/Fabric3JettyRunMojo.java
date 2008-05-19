package org.fabric3.jetty.plugin.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.codehaus.plexus.util.FileUtils;
import org.mortbay.jetty.plugin.Jetty6RunMojo;

/**
 *
 * Mojo to setup the Fabric3 extensions and boot, so that the source can be deployed on Jetty Server.
 *
 * @extendsPlugin maven-jetty-plugin
 * @goal run
 * @requiresDependencyResolution runtime
 * @execute phase="test-compile"
 * @description Runs jetty6 directly from a maven project
 */
public class Fabric3JettyRunMojo extends Jetty6RunMojo {

	/**
	 * Fabric3 path.
	 */
	private static final String FABRIC3_PATH = "\\WEB-INF\\fabric3";

	/**
	 * Fabric3 boot path.
	 */
	private static final String BOOT_PATH = FABRIC3_PATH + "\\boot";

	/**
	 * Fabric3 boot path.
	 */
	private static final String EXTENSIONS_PATH = FABRIC3_PATH + "\\extensions";

	/**
	 * The version of the runtime to use.
	 *
	 * @parameter expression="0.5"
	 */
	public String runTimeVersion;

	/**
	 * Set of extension artifacts that should be deployed to the runtime.
	 *
	 * @parameter
	 */
	public Dependency[] extensions;

	/**
	 * Set of extension artifacts that should be deployed to the runtime.
	 *
	 * @parameter
	 */
	public Dependency[] shared;

	/**
	 * Used to look up Artifacts in the remote repository.
	 *
	 * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
	 * @required
	 * @readonly
	 */
	public ArtifactResolver resolver;

	/**
	 * Used to look up Artifacts in the remote repository.
	 *
	 * @parameter expression="${component.org.apache.maven.artifact.metadata.ArtifactMetadataSource}"
	 * @required
	 * @readonly
	 */
	public ArtifactMetadataSource metadataSource;

	/**
	 * Location of the local repository.
	 *
	 * @parameter expression="${localRepository}"
	 * @readonly
	 * @required
	 */
	public ArtifactRepository localRepository;

	/**
	 * List of Remote Repositories used by the resolver
	 *
	 * @parameter expression="${project.remoteArtifactRepositories}"
	 * @readonly
	 * @required
	 */
	public List remoteRepositories;

	/**
	 * Used to look up Artifacts in the remote repository.
	 *
	 * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
	 * @required
	 * @readonly
	 */
	public ArtifactFactory artifactFactory;

	/*
	 * (non-Javadoc)
	 *
	 * @see org.mortbay.jetty.plugin.Jetty6RunMojo#execute()
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		super.execute();
	}

	/**
	 * Overriden from the base class, so as to set up the Boot and extensions.
	 * @see org.mortbay.jetty.plugin.AbstractJettyRunMojo#getClassPathFiles()
	 */
	@Override
	public List getClassPathFiles() {

		try {
			List<File> list = super.getClassPathFiles();
			setUpBootRuntime();
			setUpExtensions();
			return list;
		} catch (MojoExecutionException e) {
			throw new RuntimeException(e.getMessage());
		}

	}

	/**
	 * Sets up the extension directory
	 *
	 * @throws MojoExecutionException
	 */
	private void setUpExtensions() throws MojoExecutionException {
		List<File> bootLibs = new ArrayList<File>();
		addExtensions(bootLibs);
		File bootDir = new File(getWebAppSourceDirectory(), EXTENSIONS_PATH);
		try {
			for (File bootFile : bootLibs) {
				FileUtils.copyFileToDirectory(bootFile, bootDir);
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

	/**
	 * Sets up the boot runitme.
	 * @throws MojoExecutionException
	 */
	private void setUpBootRuntime() throws MojoExecutionException {
		List<File> bootLibs = new ArrayList<File>();
		addWebappRuntime(bootLibs);
		File bootDir = new File(getWebAppSourceDirectory(), BOOT_PATH);
		try {
			for (File bootFile : bootLibs) {
				FileUtils.copyFileToDirectory(bootFile, bootDir);
			}
		} catch (IOException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}

	}

	/**
	 * Method to add the fabric3-webapp-host dependency on the Boot Classpath.
	 *
	 * @param classpath
	 * @throws MojoExecutionException
	 */
	private void addWebappRuntime(List<File> classpath)
			throws MojoExecutionException {
		Set<Artifact> artifacts = new HashSet<Artifact>();
		List<Exclusion> exclusions = Collections.emptyList();
		Dependency dependency = new Dependency();
		dependency.setGroupId("org.codehaus.fabric3.webapp");
		dependency.setArtifactId("fabric3-webapp-host");
		dependency.setVersion(runTimeVersion);
		dependency.setExclusions(exclusions);
		addArtifacts(artifacts, dependency);

		Iterator<Artifact> artifactIterator = artifacts.iterator();

		while (artifactIterator.hasNext()) {
			Artifact artifact = artifactIterator.next();
			classpath.add(artifact.getFile());
		}
	}

	/**
	 * Resolves the dependency version, in case the versions are provided in the dependencyManagement of parent POM.
	 * @param extension
	 */
	private void resolveDependencyVersion(Dependency extension) {

		List<org.apache.maven.model.Dependency> dependencies = getProject()
				.getDependencyManagement().getDependencies();
		for (org.apache.maven.model.Dependency dependecy : dependencies) {
			if (dependecy.getGroupId().equals(extension.getGroupId())
					&& dependecy.getArtifactId().equals(
							extension.getArtifactId())) {
				extension.setVersion(dependecy.getVersion());

			}
		}
	}

	/**
	 * Resolves the dependency transitively and adds to the current artifacts List.
	 * @param artifacts list to which the dependencies will be added.
	 * @param extension dependency to resolve.
	 * @throws MojoExecutionException
	 */
	private void addArtifacts(Set<Artifact> artifacts, Dependency extension)
			throws MojoExecutionException {

		if (extension.getVersion() == null) {
			resolveDependencyVersion(extension);
		}
		final List<Exclusion> exclusions = extension.getExclusions();

		Artifact artifact = createArtifact(extension);
		try {
			resolver.resolve(artifact, remoteRepositories, localRepository);
			ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact,
					localRepository, remoteRepositories);
			ArtifactFilter filter = new ArtifactFilter() {

				public boolean include(Artifact artifact) {
					String groupId = artifact.getGroupId();
					String artifactId = artifact.getArtifactId();

					for (Exclusion exclusion : exclusions) {
						if (artifactId.equals(exclusion.getArtifactId())
								&& groupId.equals(exclusion.getGroupId())) {
							return false;
						}
					}
					return true;
				}

			};
			ArtifactResolutionResult result = resolver.resolveTransitively(
					resolutionGroup.getArtifacts(), artifact, Collections
							.emptyMap(), localRepository, remoteRepositories,
					metadataSource, filter);
			@SuppressWarnings("unchecked")
			Set<Artifact> resolvedArtifacts = result.getArtifacts();
			artifacts.add(artifact);
			artifacts.addAll(resolvedArtifacts);
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactNotFoundException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		} catch (ArtifactMetadataRetrievalException e) {
			throw new MojoExecutionException(e.getMessage(), e);
		}
	}

	/**
	 *  Creates an artifact in the repository.
	 * @param dependency
	 * @return
	 */
	private Artifact createArtifact(Dependency dependency) {
		return artifactFactory.createArtifact(dependency.getGroupId(),
				dependency.getArtifactId(), dependency.getVersion(),
				Artifact.SCOPE_RUNTIME, dependency.getType());
	}

	/**
	 * Add extensions to the ClassPath.
	 * @param classPathFiles
	 * @throws MojoExecutionException
	 */
	private void addExtensions(List<File> classPathFiles)
			throws MojoExecutionException {
		if (extensions == null) {
			return;
		}

		for (Dependency dependency : extensions) {
			if (dependency.getVersion() == null) {
				resolveDependencyVersion(dependency);
			}
			Artifact artifact = createArtifact(dependency);
			try {
				resolver.resolve(artifact, remoteRepositories, localRepository);
				classPathFiles.add(artifact.getFile());
			} catch (ArtifactResolutionException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			} catch (ArtifactNotFoundException e) {
				throw new MojoExecutionException(e.getMessage(), e);
			}
		}
	}
}
