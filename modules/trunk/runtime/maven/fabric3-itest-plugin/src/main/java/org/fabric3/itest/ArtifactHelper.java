/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.itest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
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
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Exclusion;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.fabric3.featureset.FeatureSet;

/**
 *
 * @version $Revision$ $Date$
 */
public class ArtifactHelper {

    public ArtifactFactory artifactFactory;
    public ArtifactResolver resolver;
    public ArtifactMetadataSource metadataSource;
    
    private MavenProject project;
    private ArtifactRepository localRepository;
    private List<?> remoteRepositories;
    
    /**
     * Sets the local repository to use.
     * 
     * @param localRepository Local repository to use.
     */
    public void setLocalRepository(ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }
    
    /**
     * Sets the maven project to use.
     * 
     * @param project Maven project to use.
     */
    public void setProject(MavenProject project) {
        this.project = project;
        this.remoteRepositories = project.getRemoteArtifactRepositories();
    }

    public Set<Artifact> calculateRuntimeArtifacts(String runtimeVersion) throws MojoExecutionException {
        
        List<Exclusion> exclusions = Collections.emptyList();
        Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.fabric3");
        dependency.setArtifactId("fabric3-maven-host");
        dependency.setVersion(runtimeVersion);
        dependency.setExclusions(exclusions);

        return resolveAll(dependency);
        
    }

    /**
     * Calculates module dependencies based on the set of project artifacts. Module dependencies must be visible to implementation code in a composite
     * and encompass project artifacts minus artifacts provided by the host classloader and those that are "provided scope".
     *
     * @param projectArtifacts the artifact set to determine module dependencies from
     * @param hostArtifacts    the set of host artifacts
     * @return the set of URLs pointing to module depedencies.
     */
    public Set<URL> calculateModuleDependencies(Set<Artifact> projectArtifacts, Set<Artifact> hostArtifacts) {
        Set<URL> urls = new LinkedHashSet<URL>();
        for (Artifact artifact : projectArtifacts) {
            try {
                if (hostArtifacts.contains(artifact) || Artifact.SCOPE_PROVIDED.equals(artifact.getScope()) || "f3-extension".equals(artifact.getScope())) {
                    continue;
                }
                File pathElement = artifact.getFile();
                URL url = pathElement.toURI().toURL();
                urls.add(url);

            } catch (MalformedURLException e) {
                // toURI should have encoded the URL
                throw new AssertionError(e);
            }

        }
        return urls;
    }

    public Set<Artifact> calculateDependencies() throws MojoExecutionException {
        // add all declared project dependencies
        Set<Artifact> artifacts = new HashSet<Artifact>();
        List<?> dependencies = project.getDependencies();
        for (int i = 0;i < dependencies.size();i++) {
            Dependency dependency = (Dependency) dependencies.get(i);
            if (!dependency.getScope().equals("f3-extension")) {
                artifacts.addAll(resolveAll(dependency));
            }
        }

        // include any artifacts that have been added by other plugins (e.g. Clover see FABRICTHREE-220)
        Iterator<?> it = project.getDependencyArtifacts().iterator();
        while (it.hasNext()) {
            Artifact artifact = (Artifact) it.next();
            if (!artifact.getScope().equals("f3-extension")) {
                artifacts.add(artifact);
            }
        }
        return artifacts;
    }

    /**
     * Transitively calculates the set of artifacts to be included in the host classloader based on the artifacts associated with the Maven module.
     *
     * @param runtimeArtifacts the artifacts associated with the Maven module
     * @return set of artifacts to be included in the host classloader
     * @throws MojoExecutionException if an error occurs calculating the transitive dependencies
     */
    public Set<Artifact> calculateHostArtifacts(Set<Artifact> runtimeArtifacts, 
                                                Dependency[] shared,
                                                List<FeatureSet> featureSets) throws MojoExecutionException {
        
        Set<Artifact> hostArtifacts = new HashSet<Artifact>();
        List<Exclusion> exclusions = Collections.emptyList();
        // find the version of fabric3-api being used by the runtime
        String version = null;
        for (Artifact artifact : runtimeArtifacts) {
            if ("org.codehaus.fabric3".equals(artifact.getGroupId())
                    && "fabric3-api".equals(artifact.getArtifactId())) {
                version = artifact.getVersion();
                break;
            }
        }
        if (version == null) {
            throw new MojoExecutionException("org.codehaus.fabric3:fabric3-api version not found");
        }
        // add transitive dependencies of fabric3-api to the list of artifacts in the host classloader
        Dependency fabric3Api = new Dependency();
        fabric3Api.setGroupId("org.codehaus.fabric3");
        fabric3Api.setArtifactId("fabric3-api");
        fabric3Api.setVersion(version);
        fabric3Api.setExclusions(exclusions);
        hostArtifacts.addAll(resolveAll(fabric3Api));

        // add commons annotations dependency
        Dependency jsr250API = new Dependency();
        jsr250API.setGroupId("org.apache.geronimo.specs");
        jsr250API.setArtifactId("geronimo-annotation_1.0_spec");
        jsr250API.setVersion("1.1");
        jsr250API.setExclusions(exclusions);
        hostArtifacts.addAll(resolveAll(jsr250API));

        // add shared artifacts to the host classpath
        if (shared != null) {
            for (Dependency sharedDependency : shared) {
                hostArtifacts.addAll(resolveAll(sharedDependency));
            }
        }
        
        for (FeatureSet featureSet : featureSets) {
        	for (Dependency sharedLibrary : featureSet.getSharedLibraries()) {
                hostArtifacts.addAll(resolveAll(sharedLibrary));
        	}
        }
        return hostArtifacts;
    }
    
    /**
     * Resolves the root dependency to the local artifact.
     * 
     * @param dependency Root dependency.
     * @return Resolved artifact.
     * @throws MojoExecutionException if unable to resolve any dependencies.
     */
    public Artifact resolve(Dependency dependency) throws MojoExecutionException {
        return  resolveArtifacts(dependency, false).iterator().next();
    }
    
    /**
     * Resolves all the dependencies transitively to local artifacts.
     * 
     * @param dependency Root dependency.
     * @return Resolved set of artifacts.
     * @throws MojoExecutionException if unable to resolve any dependencies.
     */
    private Set<Artifact> resolveAll(Dependency dependency) throws MojoExecutionException {
        return resolveArtifacts(dependency, true);
    }
    
    private Set<Artifact> resolveArtifacts(Dependency dependency, boolean transitive) throws MojoExecutionException {
        
        Set<Artifact> artifacts = new HashSet<Artifact>();

        if (dependency.getVersion() == null) {
            resolveDependencyVersion(dependency);
        }
        final List<?> exclusions = dependency.getExclusions();

        Artifact rootArtifact = createArtifact(dependency);
        
        try {
            
            resolver.resolve(rootArtifact, remoteRepositories, localRepository);
            artifacts.add(rootArtifact);
            
            if (!transitive) {
                return artifacts;
            }
            
            Set<Artifact> resolvedArtifacts = resolveTransitive(exclusions, rootArtifact);
            artifacts.addAll(resolvedArtifacts);
            
            return artifacts;
            
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }

    @SuppressWarnings("unchecked")
    private Set<Artifact> resolveTransitive(final List<?> exclusions, Artifact rootArtifact) throws MojoExecutionException {
        
        try {
        
            ResolutionGroup resolutionGroup = metadataSource.retrieve(rootArtifact, localRepository, remoteRepositories);
            ArtifactFilter filter = new ArtifactFilter() {
    
                public boolean include(Artifact artifact) {
                    String groupId = artifact.getGroupId();
                    String artifactId = artifact.getArtifactId();
    
                    for (int i = 0; i < exclusions.size();i++) {
                        Exclusion exclusion = (Exclusion) exclusions.get(i);
                        if (artifactId.equals(exclusion.getArtifactId()) && groupId.equals(exclusion.getGroupId())) {
                            return false;
                        }
                    }
                    return true;
                }
    
            };
            return (Set<Artifact>) resolver.resolveTransitively(resolutionGroup.getArtifacts(),
                                                                rootArtifact,
                                                                Collections.emptyMap(),
                                                                localRepository,
                                                                remoteRepositories,
                                                                metadataSource,
                                                                filter).getArtifacts();
            
        } catch (ArtifactResolutionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactNotFoundException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ArtifactMetadataRetrievalException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }
    
    /*
     * Resolves the dependency version from the projects managed dependencies.
     */
    private void resolveDependencyVersion(Dependency dependency) {

        List<?> managedDependencies = project.getDependencyManagement().getDependencies();
        for ( int i = 0;i < managedDependencies.size();i++) {
            Dependency managedDependency = (Dependency) managedDependencies.get(i);
            if (managedDependency.getGroupId().equals(dependency.getGroupId())
                    && managedDependency.getArtifactId().equals(dependency.getArtifactId())) {
                dependency.setVersion(managedDependency.getVersion());

            }
        }
    }

    /*
     * Creates an artifact from the dependency.
     */
    private Artifact createArtifact(Dependency dependency) {
        return artifactFactory.createArtifact(dependency.getGroupId(),
                                              dependency.getArtifactId(),
                                              dependency.getVersion(),
                                              Artifact.SCOPE_RUNTIME,
                                              dependency.getType());
    }

}
