package org.fabric3.jetty.plugin.impl;

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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.mortbay.jetty.plugin.Jetty6RunMojo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

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
     * The lib path, used to output the f3Extensions.properties and f3UserExtensions.properties files.
     */
    private static final String LIB_PATH = "\\WEB-INF\\lib";

	/**
	 * The version of the runtime to use.
	 *
	 * @parameter expression="0.7"
	 */
	public String runTimeVersion;

	/**
	 * Set of extension artifacts that should be deployed to the runtime.
	 *
	 * @parameter
	 */
	public Dependency[] extensions;

    /**
     * Set of extension artifacts that should be deployed to the runtime expressed as feature sets.
     *
     * @parameter
     */
    public Dependency[] features;

    /**
     * Whether to exclude default features.
     *
     * @parameter
     */
    public boolean excludeDefaultFeatures;

    /**
     * Exclude any embedded dependencies from extensions.
     *
     * @parameter
     */
    public List<String> excludes = new LinkedList<String>();

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

    /**
     * The directory for the generated WAR.
     *
     * @parameter
     */
    public Dependency[] bootLibs;

    private File bootDir;

    private DocumentBuilder db;

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
        // Ensure that we have a target directory for our various properties files
        bootDir = new File(getWebAppSourceDirectory(), LIB_PATH);
        if (!bootDir.exists()) {
            bootDir.mkdirs();
        }

        try {
            // Create a document builder
            db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        	List<File> list = super.getClassPathFiles();
			list.addAll(installRuntime());
			list.addAll(installExtensions());
//            list.addAll(setUpExtensions(userExtensions, libTargetPath, new File(libTargetPath, "f3UserExtensions.properties")));
//            list.addAll(setUpShared());
            return list;
		} catch (MojoExecutionException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (ParserConfigurationException e) {
			throw new RuntimeException(e);
		}

	}

    private List<File> installRuntime() throws MojoExecutionException, IOException {

        getLog().info("Using fabric3 runtime version " + runTimeVersion);

        if (bootLibs == null) {
            Dependency dependancy = new Dependency("org.codehaus.fabric3.webapp", "fabric3-webapp-host", runTimeVersion);
            bootLibs = new Dependency[]{dependancy};
        }

        List<File> result = new ArrayList<File>();
        for (Dependency dependency : bootLibs) {
            if (dependency.getVersion() == null) {
                resolveDependencyVersion(dependency);
            }
            for (Artifact artifact : resolveArtifact(dependency.getArtifact(artifactFactory), true)) {
                FileUtils.copyFileToDirectoryIfModified(artifact.getFile(), bootDir);
                result.add(new File(bootDir, artifact.getFile().getName()));
            }
        }

        return result;
    }

    private List<File> installExtensions() throws MojoExecutionException {
        List<File> result = new ArrayList<File>();

        try {

            Set<Dependency> uniqueExtensions = new HashSet<Dependency>();
            if (extensions != null) {
                for (Dependency extension : extensions) {
                    if (extension.getVersion() == null) {
                        resolveDependencyVersion(extension);
                    }
                    uniqueExtensions.add(extension);
                }
            }

            List<Dependency> featuresToInstall = getFeaturesToInstall();

            if (!featuresToInstall.isEmpty()) {
                for (Dependency feature : featuresToInstall) {
                    if (feature.getVersion() == null) {
                        resolveDependencyVersion(feature);
                    }
                    Artifact featureArtifact = feature.getArtifact(artifactFactory);
                    featureArtifact = resolveArtifact(featureArtifact, false).iterator().next();

                    Document featureSetDoc = db.parse(featureArtifact.getFile());

                    NodeList extensionList = featureSetDoc.getElementsByTagName("extension");

                    for (int i = 0; i < extensionList.getLength(); i++) {

                        Element extensionElement = (Element) extensionList.item(i);

                        Element artifactIdElement = (Element) extensionElement.getElementsByTagName("artifactId").item(0);
                        Element groupIdElement = (Element) extensionElement.getElementsByTagName("groupId").item(0);
                        Element versionElement = (Element) extensionElement.getElementsByTagName("version").item(0);

                        Dependency extension =
                                new Dependency(groupIdElement.getTextContent(), artifactIdElement.getTextContent(), versionElement.getTextContent());

                        uniqueExtensions.add(extension);

                    }
                }
            }
            result.addAll(processExtensions(bootDir, "f3Extensions.properties", uniqueExtensions));

        } catch (SAXParseException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        return result;
    }

    private List<Dependency> getFeaturesToInstall() {
        List<Dependency> featuresToInstall = new ArrayList<Dependency>();

        if (features != null) {
            featuresToInstall.addAll(Arrays.asList(features));
        }
        if (!excludeDefaultFeatures) {
            Dependency dependency = new Dependency("org.codehaus.fabric3", "fabric3-default-webapp-feature", runTimeVersion);
            dependency.setType("xml");
            featuresToInstall.add(dependency);
        }
        return featuresToInstall;
    }

    private List<File> processExtensions(File extensionsDir, String extensionProperties, Set<Dependency> extensions) throws MojoExecutionException {
        List<File> result = new ArrayList<File>();

        try {
            Properties props = new Properties();
            
            // process Maven dependencies
            for (Dependency dependency : extensions) {

                if (dependency.getVersion() == null) {
                    resolveDependencyVersion(dependency);
                }

                Artifact extensionArtifact = dependency.getArtifact(artifactFactory);
                extensionArtifact = resolveArtifact(extensionArtifact, false).iterator().next();

                File deflatedExtensionFile = new File(extensionsDir, extensionArtifact.getFile().getName());
                JarOutputStream deflatedExtensionOutputStream = new JarOutputStream(new FileOutputStream(deflatedExtensionFile));

                JarFile extensionFile = new JarFile(extensionArtifact.getFile());
                Enumeration<JarEntry> entries = extensionFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    String entryName = jarEntry.getName();
                    if (entryName.startsWith("META-INF/lib") && entryName.endsWith(".jar")) {
                        String extractedLibraryName = entryName.substring(entryName.lastIndexOf('/') + 1);
                        if (excludes.contains(extractedLibraryName)) {
                            continue;
                        }
                        File extractedLibraryFile = new File(extensionsDir, extractedLibraryName);
                        if (!extractedLibraryFile.exists()) {
                            FileOutputStream outputStream = new FileOutputStream(extractedLibraryFile);
                            InputStream inputStream = extensionFile.getInputStream(jarEntry);
                            IOUtil.copy(inputStream, outputStream);
                            IOUtil.close(inputStream);
                            IOUtil.close(outputStream);
                        }
                        result.add(extractedLibraryFile);
                    } else {
                        deflatedExtensionOutputStream.putNextEntry(jarEntry);
                        InputStream inputStream = extensionFile.getInputStream(jarEntry);
                        IOUtil.copy(inputStream, deflatedExtensionOutputStream);
                        IOUtil.close(inputStream);
                    }
                }

                IOUtil.close(deflatedExtensionOutputStream);
                result.add(deflatedExtensionFile);

                props.put(extensionArtifact.getFile().getName(), extensionArtifact.getFile().getName());

            }

            props.store(new FileOutputStream(new File(extensionsDir, extensionProperties)), null);

            return result;
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

    }

    /**
     * Resolve the dependency for the given extension from the dependencyManagement from the pom
     *
     * @param extension the dependcy information for the extension
     */
    @SuppressWarnings({"unchecked"})
    private void resolveDependencyVersion(Dependency extension) {
        List<org.apache.maven.model.Dependency> dependencies = getProject().getDependencyManagement().getDependencies();
        for (org.apache.maven.model.Dependency dependecy : dependencies) {
            if (dependecy.getGroupId().equals(extension.getGroupId())
                    && dependecy.getArtifactId().equals(extension.getArtifactId())) {
                extension.setVersion(dependecy.getVersion());

            }
        }
    }

    /**
     * Resolves the specified artifact.
     *
     * @param artifact   Artifact to be resolved.
     * @param transitive Whether to resolve transitively.
     * @return A set of resolved artifacts.
     * @throws MojoExecutionException if there is an error resolving the artifact
     */
    private Set<Artifact> resolveArtifact(Artifact artifact, boolean transitive) throws MojoExecutionException {

        try {
            Set<Artifact> resolvedArtifacts = new HashSet<Artifact>();

            // Resolve the artifact
            resolver.resolve(artifact, remoteRepositories, localRepository);
            resolvedArtifacts.add(artifact);

            if (!transitive) {
                return resolvedArtifacts;
            }

            // Transitively resolve all the dependencies
            ResolutionGroup resolutionGroup = metadataSource.retrieve(artifact, localRepository, remoteRepositories);
            ArtifactResolutionResult result = resolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                                           artifact,
                                                                           remoteRepositories,
                                                                           localRepository,
                                                                           metadataSource);

            // Add the artifacts to the deployment unit
            for (Object depArtifact : result.getArtifacts()) {
                resolvedArtifacts.add((Artifact) depArtifact);
            }
            return resolvedArtifacts;

        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
