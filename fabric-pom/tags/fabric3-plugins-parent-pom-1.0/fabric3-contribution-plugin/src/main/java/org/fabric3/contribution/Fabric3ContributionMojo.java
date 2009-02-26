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
package org.fabric3.contribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.archiver.jar.JarArchiver;

/**
 * Builds an SCA contribution. Two contribution types are currently supported, ZIP- and XML-based.
 * <p/>
 * Contribution archives may be jars or zip files as specified by the respective Maven packaging entries,
 * <code>&lt;packaging&gt;sca-contribution-jar&lt;/packaging&gt;</code> and <code>&lt;packaging&gt;sca-contribution-jar&lt;/packaging&gt;</code>. Any
 * required project dependencies (e.g. not scoped as provided) that are not themselves SCA contributions will be added to the archive's META-INF/lib
 * directory, making them available to the contribution and runtime extension classpaths.
 * <p/>
 * The following is an example plugin configuration:
 * <pre>
 * 
 * &lt;build&gt;
 *   &lt;plugins&gt;
 *     &lt;plugin&gt;
 *       &lt;groupId&gt;org.codehaus.fabric3&lt;/groupId&gt;
 *       &lt;artifactId&gt;fabric3-contribution-plugin&lt;/artifactId&gt;
 *       &lt;version&gt;RELEASE&lt;/version&gt;
 *       &lt;extensions&gt;true&lt;/extensions&gt;
 *      &lt;/plugin&gt;
 *    &lt;/plugins&gt;
 *  &lt;/build&gt;
 * </pre>
 *
 * @version $Rev$ $Date$
 * @goal package
 * @phase package
 */
public class Fabric3ContributionMojo extends AbstractMojo {
    private static final String JAR_PACKAGING = "sca-contribution-jar";

    private static final String[] DEFAULT_EXCLUDES = new String[]{"**/package.html"};
    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * Name of the generated composite archive.
     *
     * @parameter expression="${project.build.finalName}"
     */
    protected String contributionName;

    /**
     * Classifier to add to the generated artifact.
     *
     * @parameter
     */
    protected String classifier;

    /**
     * Directory containing the classes to include in the archive.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    protected File classesDirectory;

    /**
     * Standard Maven archive configuration.
     *
     * @parameter
     */
    protected MavenArchiveConfiguration archive = new MavenArchiveConfiguration();

    /**
     * The Jar archiver.
     *
     * @parameter expression="${component.org.codehaus.plexus.archiver.Archiver#jar}"
     * @required
     * @readonly
     */
    protected JarArchiver jarArchiver;

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * @component
     * @required
     * @readonly
     */
    protected MavenProjectHelper projectHelper;

    /**
     * @parameter expression="${project.packaging}
     * @required
     * @readonly
     */
    protected String packaging;

    /**
     * @parameter
     */
    protected File composite;

    /**
     * @parameter
     */
    protected String compositeFile = "composite.composite";

    public void execute() throws MojoExecutionException, MojoFailureException {
        // the project packaging is set to output a ZIP-based contribution
        File contribution = createArchive();
        // set the contribution file for Maven
        if (classifier != null) {
            projectHelper.attachArtifact(project, "f3r", classifier, contribution);
        } else {
            project.getArtifact().setFile(contribution);
        }

    }

    /**
     * Outputs a ZIP contribution.
     *
     * @return a File pointing to the created archive
     * @throws MojoExecutionException if an error occurs generating the contribution
     */
    private File createArchive() throws MojoExecutionException {

        File contribution = getJarFile(contributionName, classifier);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(contribution);
        archive.setForced(true);

        try {
            if (!classesDirectory.exists()) {
                throw new FileNotFoundException(String.format("Unable to package contribution, %s does not exist.", classesDirectory));
            } else {
                includeDependencies();
                archiver.getArchiver().addDirectory(classesDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
            }

            archiver.createArchive(project, archive);

            return contribution;
        }
        catch (Exception e) {
            throw new MojoExecutionException("Error assembling contribution", e);
        }

    }

    /**
     * Returns a File representing the name and location of the archive file to output.
     *
     * @param name       the archive name
     * @param classifier the classifier
     * @return a File representing the name and location of the archive file to output
     */
    private File getJarFile(String name, String classifier) {

        getLog().debug("Calculating the archive file name");
        if (classifier != null) {
            classifier = classifier.trim();
            if (classifier.length() > 0) {
                name = name + '-' + classifier;
            }
        }
        String extension = ".zip";
        if (JAR_PACKAGING.endsWith(packaging)) {
            extension = ".jar";
        }
        return new File(outputDirectory, name + extension);

    }

    /**
     * Copies all transitive dependencies to the output archive that are required for runtime operation, excluding other SCA contributions as they
     * will be deployed separately.
     *
     * @throws IOException if an error occurs copying the dependencies
     */
    private void includeDependencies() throws IOException {
        getLog().debug("Including dependencies in archive");
        File libDir = new File(classesDirectory, "META-INF" + File.separator + "lib");
        ScopeArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = (Set<Artifact>) project.getArtifacts();
        for (Artifact artifact : artifacts) {
            getLog().debug("checking " + artifact.getArtifactId());
            boolean isSCAContribution = artifact.getType().startsWith("sca-contribution");
            if (!isSCAContribution && !artifact.isOptional() && filter.include(artifact)) {
                getLog().debug(String.format("including dependency %s", artifact));
                File destinationFile = new File(libDir, artifact.getFile().getName());
                if (!libDir.exists()) {
                    libDir.mkdirs();
                }
                getLog().debug(String.format("copying %s to %s", artifact.getFile(), destinationFile));
                FileChannel destChannel = new FileOutputStream(destinationFile).getChannel();
                FileChannel srcChannel = new FileInputStream(artifact.getFile()).getChannel();
                srcChannel.transferTo(0, srcChannel.size(), destChannel);
                destChannel.close();
                srcChannel.close();
            }
        }

    }

}