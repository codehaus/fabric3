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
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;

/**
 * Add fabric3 runtime dependencies to a webapp.
 * <p/>
 * Performs the following tasks.
 * <p/>
 * <ul> <li>Adds the boot dependencies transitively to WEB-INF/fabric3/boot</li> <li>By default boot libraries are
 * transitively resolved from webapp-host</li> <li>The version of boot libraries can be specified using
 * configuration/runTimeVersion element</li> <li>Boot libraries can be overridden using the configuration/bootLibs
 * element in the plugin</li> <li>Adds the extension artifacts specified using configuration/extensions to
 * WEB-INF/fabric3/boot</li> </ul>
 *
 * @version $Rev$ $Date$
 * @goal fabric3-war
 * @phase generate-resources
 */
public class Fabric3WarMojo extends AbstractMojo {

    /**
     * Fabric3 path.
     */
    private static final String FABRIC3_PATH = "WEB-INF/fabric3";

    /**
     * Fabric3 boot path.
     */
    private static final String BOOT_PATH = FABRIC3_PATH + "/boot";

    /**
     * Fabric3 boot path.
     */
    private static final String EXTENSIONS_PATH = FABRIC3_PATH + "/extensions";

    /**
     * The directory where the webapp is built.
     *
     * @parameter expression="${project.build.directory}/${project.build.finalName}"
     * @required
     */
    public File webappDirectory;

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
     * The spi dependencies.
     *
     * @parameter
     */
    public Dependency[] spiLibs;

    /**
     * Set of extension artifacts that should be deployed to the runtime.
     *
     * @parameter
     */
    public Dependency[] extensions;

    /**
     * The default version of the runtime to use.
     *
     * @parameter
     */
    public String runTimeVersion;

    /**
     * The default version of the spi to use.
     *
     * @parameter
     */
    public String spiVersion;

    /**
     * Executes the MOJO.
     */
    public void execute() throws MojoExecutionException {
        if (runTimeVersion == null) {
            try {
                runTimeVersion = getPluginVersion();
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
        getLog().info("Using fabric3 runtime version " + runTimeVersion);

        if (bootLibs == null) {
            Dependency dependancy = new Dependency("org.codehaus.fabric3.webapp", "webapp-host", runTimeVersion);
            bootLibs = new Dependency[]{dependancy};
        }

        if (spiLibs == null) {
            String version;
            if (spiVersion == null) {
                version = runTimeVersion;
            } else {
                version = spiVersion;
            }
            Dependency dep1 = new Dependency("org.codehaus.fabric3", "fabric3-extension", version);
            Dependency dep2 = new Dependency("stax", "stax-api", "1.0");
            Dependency dep3 = new Dependency("org.codehaus.woodstox", "wstx-asl", "3.2.0");
            Dependency dep4 = new Dependency("org.apache.tuscany", "commonj-api_r1.1", "1.0-incubator-M2");
            Dependency dep5 = new Dependency("org.apache.geronimo.specs", "geronimo-j2ee-connector_1.5_spec", "1.0.1");
            spiLibs = new Dependency[]{dep1, dep2, dep3, dep4, dep5};
        }
        try {
            Set<Artifact> spiArtifacts = new HashSet<Artifact>();
            for (Dependency dependency : spiLibs) {
                Set<Artifact> artifacts = resolveArtifact(dependency.getArtifact(artifactFactory), true);
                spiArtifacts.addAll(artifacts);
            }
            File bootDir = new File(webappDirectory, BOOT_PATH);
            bootDir.mkdirs();
            for (Dependency dependency : bootLibs) {
                for (Artifact artifact : resolveArtifact(dependency.getArtifact(artifactFactory), true)) {
                    // FIXME use this when maven updates to plexus-utils-1.4
                    // FileUtils.copyFileToDirectoryIfModified(bootDir, artifact.getFile());
                    if (!spiArtifacts.contains(artifact)) {
                        // don't copy dependencies related to SPIs as those are loaded in the host classloader
                        copyFileToDirectoryIfModified(artifact.getFile(), bootDir);
                    }
                }
            }
            File libDir = new File(webappDirectory, "WEB-INF/lib");
            for (Artifact artifact : spiArtifacts) {
                copyFileToDirectoryIfModified(artifact.getFile(), libDir);
            }
            installExtensions();

            // TODO add user dependencies to the war in the way the contribution service expects
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void installExtensions() throws ArtifactNotFoundException, ArtifactResolutionException, IOException,
            ArtifactMetadataRetrievalException {
        if (extensions == null) {
            return;
        }

        File bootDir = new File(webappDirectory, EXTENSIONS_PATH);
        for (Dependency dependency : extensions) {
            for (Artifact artifact : resolveArtifact(dependency.getArtifact(artifactFactory), false)) {
                // FIXME use this when maven updates to plexus-utils-1.4
                // FileUtils.copyFileToDirectoryIfModified(bootDir, artifact.getFile());
                copyFileToDirectoryIfModified(artifact.getFile(), bootDir);
            }
        }
    }

    // Use copyFileToDirectoryIfModified from plexus-utils-1.4
    @Deprecated
    private static boolean copyFileToDirectoryIfModified(File source, File targetDir) throws IOException {
        File target = new File(targetDir, source.getName());
        if (target.lastModified() < source.lastModified()) {
            FileUtils.copyFile(source, target);
            return true;
        } else {
            return false;
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

    private String getPluginVersion() throws IOException {
        Properties pomProperties = new Properties();
        String propFile = "/META-INF/maven/org.codehaus.fabric3/fabric3-webapp-plugin/pom.properties";
        InputStream is = getClass().getResourceAsStream(propFile);
        try {
            pomProperties.load(is);
            return pomProperties.getProperty("version");
        } finally {
            IOUtil.close(is);
        }
    }
}
