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
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.MojoExecutionException;
import org.fabric3.featureset.FeatureSet;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.runtime.BootConfiguration;
import org.fabric3.host.runtime.ScdlBootstrapper;
import org.fabric3.maven.MavenEmbeddedRuntime;

/**
 * @version $Revision$ $Date$
 */
public class ExtensionHelper {

    public ArtifactHelper artifactHelper;

    public void processExtensions(BootConfiguration<MavenEmbeddedRuntime, ScdlBootstrapper> configuration,
                                  Dependency[] extensions,
                                  Set<Artifact> extensionArtifacts,
                                  List<FeatureSet> featureSets,
                                  Dependency[] userExtensions,
                                  File[] userExtensionsArchives) throws MojoExecutionException {
        List<URL> extensionUrls = resolveDependencies(extensions, extensionArtifacts);

        if (featureSets != null) {
            for (FeatureSet featureSet : featureSets) {
                extensionUrls.addAll(processFeatures(featureSet));
            }
        }
        List<ContributionSource> sources = createContributionSources(extensionUrls);
        configuration.setExtensions(sources);

        List<URL> userExtensionUrls = resolveDependencies(userExtensions, null);
        // add extensions that are not Maven artifacts
        if (userExtensionsArchives != null) {
            for (File entry : userExtensionsArchives) {
                if (!entry.exists()) {
                    throw new MojoExecutionException("User extension does not exist: " + entry);
                }
                try {
                    userExtensionUrls.add(entry.toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new MojoExecutionException("Invalid user extension URL: " + entry, e);
                }
            }
        }
        sources = createContributionSources(userExtensionUrls);
        configuration.setUserExtensions(sources);
    }

    private List<ContributionSource> createContributionSources(List<URL> urls) {
        List<ContributionSource> sources = new ArrayList<ContributionSource>();
        for (URL extensionUrl : urls) {
            // it's ok to assume archives are uniquely named since most server environments have a single deploy directory
            URI uri = URI.create(new File(extensionUrl.getFile()).getName());
            ContributionSource source = new FileContributionSource(uri, extensionUrl, -1, new byte[0]);
            sources.add(source);
        }
        return sources;
    }

    private List<URL> processFeatures(FeatureSet featureSet) throws MojoExecutionException {
        Set<Dependency> dependencies = featureSet.getExtensions();
        return resolveDependencies(featureSet.getExtensions().toArray(new Dependency[dependencies.size()]), null);
    }

    private List<URL> resolveDependencies(Dependency[] dependencies, Set<Artifact> extensionArtifacts) throws MojoExecutionException {

        List<URL> urls = new ArrayList<URL>();

        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                Artifact artifact = artifactHelper.resolve(dependency);
                try {
                    urls.add(artifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError();
                }
            }
        }
        
        if (extensionArtifacts != null) {
            for (Artifact extensionArtifact : extensionArtifacts) {
                try {
                    urls.add(extensionArtifact.getFile().toURI().toURL());
                } catch (MalformedURLException e) {
                    throw new AssertionError();
                }
            }
        }

        return urls;

    }

}
