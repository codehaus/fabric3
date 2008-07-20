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
package org.fabric3.featureset;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * 
 * Mojo for generating a feature set from a set of requested extensions. A feature set can be built by composing a number of other feature sets, 
 * and or including a set of explicitly requested extensions. A feature set is published as maven artifact with the extension .xml. This can be later 
 * referenced by the itest and webapp plugins, instead of explictly referencing all the extensions included in the feature set. User applications are 
 * expected to have a separate maven module to build the feature set, and then the installed artifact will be reused from the other modules that use 
 * the itest and webapp plugins.
 * 
 * An example usage of the feature set plugin is shown below,
 * 
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
 *       &lt;/configuration&gt;
 *     &lt;/plugin&gt;
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public class Fabric3FeatureSetMojo extends AbstractMojo {

    /**
     *
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
     * @parameter
     */
    protected Dependency[] includes;

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

        processExtensions();
        
        processIncludes();
        
        /*
         * <featureSet>
         *     <exension>
         *         <artifactId></artifactId>
         *         <groupId></groupId>
         *         <version></version>
         *     </extension>
         * </featureSet>
         * 
         */
        
        featureSet.serialize(project.getFile());

    }

    /*
     * Processes the included feature sets.
     */
    private void processIncludes() throws MojoExecutionException {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }

        for (Dependency include : includes) {
            
            File featureSetFile = resolve(include);
            Document featureSetDoc;
            
            try {
                featureSetDoc = db.parse(featureSetFile);
            } catch (SAXException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            } catch (IOException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
            
            NodeList extensionList = featureSetDoc.getElementsByTagName("extension");
            
            for (int i = 0;i < extensionList.getLength();i++) {
                
                Element extensionElement = (Element) extensionList.item(i);
                
                String artifactId = extensionElement.getElementsByTagName("artifactId").item(0).getNodeValue();
                String groupId = extensionElement.getElementsByTagName("groupId").item(0).getNodeValue();
                String version = extensionElement.getElementsByTagName("version").item(0).getNodeValue();
                
                Dependency extension = new Dependency();
                extension.setArtifactId(artifactId);
                extension.setGroupId(groupId);
                extension.setVersion(version);
                
                resolve(extension);
                featureSet.addExtension(extension);
                
            }
        }
        
    }

    /*
     * Processes the requested extensions. 
     */
    private void processExtensions() throws MojoExecutionException {
        for (Dependency extension : extensions) {
            resolve(extension);
            featureSet.addExtension(extension);
        }
    }

    /*
     * Resolves the depnednecy to anartifact file in the repository.
     */
    private File resolve(Dependency dep) throws MojoExecutionException {

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
    private Artifact createArtifact(Dependency dep) {
        return artifactFactory.createArtifact(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), Artifact.SCOPE_RUNTIME, dep.getType());
    }

    /*
     * Resolves the dependency version, if the version is not specified.
     */
    @SuppressWarnings("unchecked")
    private void resolveDependencyVersion(Dependency dep) {

        List<org.apache.maven.model.Dependency> dependencies = project.getDependencyManagement().getDependencies();
        for (org.apache.maven.model.Dependency dependecy : dependencies) {
            if (dependecy.getGroupId().equals(dep.getGroupId()) && dependecy.getArtifactId().equals(dep.getArtifactId())) {
                dep.setVersion(dependecy.getVersion());

            }
        }

    }

}
