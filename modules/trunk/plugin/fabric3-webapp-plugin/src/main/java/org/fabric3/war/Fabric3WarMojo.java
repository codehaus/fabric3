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
package org.fabric3.war;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Add fabric3 runtime dependencies to a webapp. The wenapp runtime in Fabric3 currently doesn't support classloader isolation. All JAR
 * files are added to the WEB-INF/lib directory. All system and user extensions are added to the same directory as well. The list of system 
 * extensions are specified in the properties file f3Extensions.properties and the list of user extensions are specified in the 
 * f3UserExtenion.properties.
 * 
 * Both system and user extensions are exploded and the contents of the META-INF/lib directory are copied to the WEB-INF/lib directory.
 * 
 * <p/>
 * Performs the following tasks.
 * <p/>
 * <ul> <li>Adds the boot dependencies transitively to WEB-INF/lib</li> <li>By default boot libraries are transitively resolved from
 * webapp-host</li> <li>The version of boot libraries can be specified using configuration/runTimeVersion element</li> <li>Boot libraries can be
 * overridden using the configuration/bootLibs element in the plugin</li> <li>Adds the extension artifacts specified using configuration/extensions to
 * WEB-INF/lib</li> </ul>
 *
 * @version $Rev$ $Date$
 * @goal fabric3-war
 * @phase generate-resources
 */
public class Fabric3WarMojo extends AbstractMojo {

    /**
     * Fabric3 boot path.
     */
    private static final String BOOT_PATH = "WEB-INF/lib";

    /**
     * Fabric3 extensions path.
     */
    private static final String EXTENSIONS_PATH = "WEB-INF/lib";

    /**
     * Fabric3 user extensions path.
     */
    private static final String USER_EXTENSIONS_PATH = "WEB-INF/lib";

    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    public File webappDirectory;

    /**
     * The build target directory.
     *
     * @parameter expression="${project.build.directory}/target/temp/extension"
     * @required
     */
    public File tempExtensionDirectory;

    /**
     * Artifact metadata source.
     *
     * @component
     */
    public ArtifactMetadataSource metadataSource;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    public ArtifactFactory artifactFactory;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
     * @required
     * @readonly
     */
    public ArtifactResolver resolver;

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
     * The directory for the generated WAR.
     *
     * @parameter
     */
    public Dependency[] bootLibs;

    /**
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

    /**
     * Set of user extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] userExtensions;

    /**
     * The default version of the runtime to use.
     *
     * @parameter expression="RELEASE"
     */
    public String runTimeVersion;

    /**
     * POM
     *
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    public MavenProject project;

    /**
     * Executes the MOJO.
     */
    public void execute() throws MojoExecutionException {
        try {
            installRuntime();
            installExtensions();

            // TODO add user dependencies to the war in the way the contribution service expects
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void installRuntime() throws IOException, ArtifactResolutionException, ArtifactNotFoundException,
            ArtifactMetadataRetrievalException {

        getLog().info("Using fabric3 runtime version " + runTimeVersion);

        if (bootLibs == null) {
            Dependency dependancy = new Dependency("org.codehaus.fabric3.webapp", "fabric3-webapp-host", runTimeVersion);
            bootLibs = new Dependency[]{dependancy};
        }

        File bootDir = new File(webappDirectory, BOOT_PATH);
        bootDir.mkdirs();
        for (Dependency dependency : bootLibs) {
            if (dependency.getVersion() == null) {
                resolveDependencyVersion(dependency);
            }
            for (Artifact artifact : resolveArtifact(dependency.getArtifact(artifactFactory), true)) {
                FileUtils.copyFileToDirectoryIfModified(artifact.getFile(), bootDir);
            }
        }
    }

    private void installExtensions() throws ArtifactNotFoundException, ArtifactResolutionException, IOException, ArtifactMetadataRetrievalException {
        
        if (extensions != null) {
            processExtensions(EXTENSIONS_PATH, "f3Extensions.properties");
        }
        if (userExtensions != null) {
            processExtensions(USER_EXTENSIONS_PATH, "f3UserExtensions.properties");
        }

    }

    private void processExtensions(String extenstionsPath, String extensionProperties) 
        throws IOException, ArtifactResolutionException, ArtifactNotFoundException, ArtifactMetadataRetrievalException, FileNotFoundException {
        
        tempExtensionDirectory.delete();
        tempExtensionDirectory.mkdirs();
        
        Properties props = new Properties();
        File extensionsDir = new File(webappDirectory, extenstionsPath);
        
        for (Dependency dependency : extensions) {
            
            if (dependency.getVersion() == null) {
                resolveDependencyVersion(dependency);
            }
            
            Artifact extensionArtifact = dependency.getArtifact(artifactFactory);
            for (Artifact artifact : resolveArtifact(extensionArtifact, false)) {
                
                if (artifact.equals(extensionArtifact)) {
                    
                    File deflatedExtensionFile = new File(tempExtensionDirectory, extensionArtifact.getFile().getName());
                    JarOutputStream deflatedExtensionOutputStream = new JarOutputStream(new FileOutputStream(deflatedExtensionFile));
                    
                    JarFile extensionFile = new JarFile(extensionArtifact.getFile());
                    Enumeration<JarEntry> entries = extensionFile.entries();
                    while (entries.hasMoreElements()) {
                        JarEntry jarEntry = entries.nextElement();
                        String entryName = jarEntry.getName();
                        if (entryName.startsWith("META-INF/lib") && entryName.endsWith(".jar")) {
                            String extractedLibraryName = entryName.substring(entryName.lastIndexOf('/') + 1);
                            File extractedLibraryFile = new File(tempExtensionDirectory, extractedLibraryName);
                            FileOutputStream outputStream = new FileOutputStream(extractedLibraryFile);
                            InputStream inputStream = extensionFile.getInputStream(jarEntry);
                            IOUtil.copy(inputStream, outputStream);
                            IOUtil.close(inputStream);
                            IOUtil.close(outputStream);
                            FileUtils.copyFileToDirectory(extractedLibraryFile, extensionsDir);
                        } else {
                            deflatedExtensionOutputStream.putNextEntry(jarEntry);
                            InputStream inputStream = extensionFile.getInputStream(jarEntry);
                            IOUtil.copy(inputStream, deflatedExtensionOutputStream);
                            IOUtil.close(inputStream);
                        }
                    }
                    
                    IOUtil.close(deflatedExtensionOutputStream);
                    FileUtils.copyFileToDirectory(deflatedExtensionFile, extensionsDir);
                    
                } else {
                    FileUtils.copyFileToDirectoryIfModified(artifact.getFile(), extensionsDir);
                }
                
                props.put(artifact.getFile().getName(), artifact.getFile().getName());
                
            }
        }
        
        props.store(new FileOutputStream(new File(extensionsDir, extensionProperties)), null);
        
    }

    /**
     * Resolve the dependency for the given extension from the dependencyManagement from the pom
     *
     * @param extension the dependcy information for the extension
     */
    private void resolveDependencyVersion(Dependency extension) {
        List<org.apache.maven.model.Dependency> dependencies = project.getDependencyManagement().getDependencies();
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
     * @throws IOException                 In case of an unexpected IO error.
     * @throws ArtifactResolutionException If the artifact cannot be resolved.
     * @throws ArtifactNotFoundException   If the artifact is not found.
     * @throws ArtifactMetadataRetrievalException
     *                                     In case of error in retrieving metadata.
     */
    private Set<Artifact> resolveArtifact(Artifact artifact, boolean transitive)
            throws IOException, ArtifactResolutionException,
            ArtifactNotFoundException, ArtifactMetadataRetrievalException {

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

    }
}
