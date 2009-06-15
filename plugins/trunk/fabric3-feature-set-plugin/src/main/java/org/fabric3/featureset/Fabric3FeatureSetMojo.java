/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.featureset;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.xml.sax.SAXException;

/**
 * Mojo for generating a feature set from a set of requested extensions. A feature set can be built by composing a number of other feature sets, and
 * or including a set of explicitly requested extensions. A feature set is published as maven artifact with the extension .xml. This can be later
 * referenced by the itest and webapp plugins, instead of explictly referencing all the extensions included in the feature set. User applications are
 * expected to have a separate maven module to build the feature set, and then the installed artifact will be reused from the other modules that use
 * the itest and webapp plugins. Feature sets can also contain shared dependencies used in itest environments.
 * <p/>
 * An example usage of the feature set plugin is shown below,
 * <p/>
 * <pre>
 *    &lt;plugin&gt;
 *       &lt;groupId&gt;org.codehaus.fabric3&lt;/groupId&gt;
 *       &lt;artifactId&gt;fabric3-feature-set-plugin&lt;/artifactId&gt;
 *       &lt;extensions&gt;true&lt;/extensions&gt;
 *       &lt;configuration&gt;
 *          &lt;extensions&gt;
 *             &lt;dependency&gt;
 *                &lt;groupId&gt;org.mycompanyf&lt;/groupId&gt;
 *                &lt;artifactId&gt;mycompany-extension&lt;/artifactId&gt;
 *             &lt;/dependency&gt;
 *          &lt;/extensions&gt;
 *          &lt;includes&gt;
 *             &lt;dependency&gt;
 *                &lt;groupId&gt;org.codehaus.fabric3&lt;/groupId&gt;
 *                &lt;artifactId&gt;fabric3-hibernate-feature-set&lt;/artifactId&gt;
 *             &lt;/dependency&gt;
 *          &lt;/includes&gt;
 *          &lt;shared&gt;
 *             &lt;dependency&gt;
 *                &lt;groupId&gt;javax.persistence&lt;/groupId&gt;
 *                &lt;artifactId&gt;persistence-api&lt;/artifactId&gt;
 *             &lt;/dependency&gt;
 *          &lt;/shared&gt;
 *       &lt;/configuration&gt;
 *     &lt;/plugin&gt;
 * </pre>
 *
 * @version $Rev$ $Date$
 * @goal package
 * @phase package
 */
public class Fabric3FeatureSetMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     * @readonly
     * @required
     */
    protected MavenProject project;

    /**
     * @parameter
     */
    protected Dependency[] extensions;

    /**
     * Build output directory.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    protected File outputDirectory;

    /**
     * @parameter
     */
    protected Dependency[] includes;

    /**
     * @parameter
     */
    protected Dependency[] shared;

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
    public List<String> remoteRepositories;

    /**
     * Used to look up Artifacts in the remote repository.
     *
     * @parameter expression="${component.org.apache.maven.artifact.factory.ArtifactFactory}"
     * @required
     * @readonly
     */
    public ArtifactFactory artifactFactory;

    // Feature set containing the requested extensions
    private FeatureSet featureSet = new FeatureSet();

    /**
     * Generates the feature set files.
     */
    public void execute() throws MojoExecutionException, MojoFailureException {

        String fileName = project.getArtifactId() + "-" + project.getVersion() + ".xml";
        File file = new File(outputDirectory, fileName);
        try {
            outputDirectory.mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        if (extensions == null && includes == null) {
            throw new MojoExecutionException("Extensions or includes should be specified");
        }

        processExtensions();
        processShared();
        processIncludes();

        try {
            featureSet.serialize(file);
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        project.getArtifact().setFile(file);

    }

    /*
     * Processes the included feature sets.
     */
    private void processIncludes() throws MojoExecutionException {

        if (includes == null) {
            return;
        }

        for (Dependency include : includes) {

            File featureSetFile = resolve(include);
            FeatureSet includedFeatureSet = null;
            try {
                includedFeatureSet = FeatureSet.deserialize(featureSetFile);
            } catch (ParserConfigurationException e) {
                throw new MojoExecutionException("Unable to process includes", e);
            } catch (SAXException e) {
                throw new MojoExecutionException("Unable to process includes", e);
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to process includes", e);
            }

            for (org.apache.maven.model.Dependency extension : includedFeatureSet.getExtensions()) {
                resolve(extension);
                featureSet.addExtension(extension);
            }

            for (org.apache.maven.model.Dependency sharedLibrary : includedFeatureSet.getSharedLibraries()) {
                resolve(sharedLibrary);
                featureSet.addSharedLibrary(sharedLibrary);
            }

        }

    }

    /*
     * Processes the requested extensions. 
     */
    private void processExtensions() throws MojoExecutionException {

        if (extensions == null) {
            return;
        }

        for (Dependency extension : extensions) {
            resolve(extension);
            featureSet.addExtension(extension);
        }

    }

    /*
     * Processes the requested shared libraries. 
     */
    private void processShared() throws MojoExecutionException {

        if (shared == null) {
            return;
        }

        for (Dependency sharedLibrary : shared) {
            resolve(sharedLibrary);
            featureSet.addSharedLibrary(sharedLibrary);
        }

    }

    /*
     * Resolves the depnednecy to anartifact file in the repository.
     */
    private File resolve(org.apache.maven.model.Dependency dep) throws MojoExecutionException {

        if (dep.getVersion() == null) {
            resolveDependencyVersion(dep);
        }

        Artifact artifact = createArtifact(dep);
        try {
            resolver.resolve(artifact, remoteRepositories, localRepository);
            return artifact.getFile();
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    /*
     * Creates the artifact from the dependency.
     */
    private Artifact createArtifact(org.apache.maven.model.Dependency dep) {
        return artifactFactory.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), Artifact.SCOPE_RUNTIME, dep.getType());
    }

    /*
     * Resolves the dependency version, if the version is not specified.
     */
    @SuppressWarnings("unchecked")
    private void resolveDependencyVersion(org.apache.maven.model.Dependency dep) {

        List<org.apache.maven.model.Dependency> dependencies = project.getDependencyManagement().getDependencies();
        for (org.apache.maven.model.Dependency dependecy : dependencies) {
            if (dependecy.getGroupId().equals(dep.getGroupId()) && dependecy.getArtifactId().equals(dep.getArtifactId())) {
                dep.setVersion(dependecy.getVersion());

            }
        }

    }

}
