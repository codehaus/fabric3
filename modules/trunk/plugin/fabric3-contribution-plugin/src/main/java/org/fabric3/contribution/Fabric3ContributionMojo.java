/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.contribution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Builds an archive suitable for contribution to an SCA Domain.
 *
 * @version $Rev$ $Date$
 * @goal package
 * @phase package
 */
public class Fabric3ContributionMojo extends AbstractMojo {
    
    private static final String[] DEFAULT_EXCLUDES = new String[]{"**/package.html"};
    private static final String[] DEFAULT_INCLUDES = new String[]{"**/**"};
    
    private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance();
    private static final TransformerFactory TF = TransformerFactory.newInstance();
    
    static {
        DBF.setNamespaceAware(true);
    }

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
    protected String[] deployables;
    
    /**
     * @parameter
     */
    protected MavenImport[] mavenImports;
    

    public void execute() throws MojoExecutionException, MojoFailureException {
        
        File contribution = createArchive();

        if (classifier != null) {
            projectHelper.attachArtifact(project, "f3r", classifier, contribution);
        } else {
            project.getArtifact().setFile(contribution);
        }
        
    }

    private File createArchive() throws MojoExecutionException {

        File contribution = getJarFile(outputDirectory, contributionName, classifier);

        MavenArchiver archiver = new MavenArchiver();
        archiver.setArchiver(jarArchiver);
        archiver.setOutputFile(contribution);
        archive.setForced(true);

        try {
            File contentDirectory = classesDirectory;
            if (!contentDirectory.exists()) {
                throw new FileNotFoundException(String.format("Unable to package contribution, %s does not exist.",contentDirectory));
            } else {
            	includeDependencies(contentDirectory);
            	generateScaContributionFile(contentDirectory);
            	archiver.getArchiver().addDirectory(contentDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
            }

            archiver.createArchive(project, archive);

            return contribution;
        }
        catch (Exception e) {
            throw new MojoExecutionException("Error assembling contribution", e);
        }
        
    }

    private File getJarFile(File buildDir, String finalName, String classifier) {
        
    	getLog().debug( "Calculating the archive file name");
        if (classifier != null) {
            classifier = classifier.trim();
            if (classifier.length() > 0) {
                finalName = finalName + '-' + classifier;
            }
        }
        String extension=".zip";
        if ("sca-contribution-jar".endsWith(packaging)){
            extension=".jar";
        }
        return new File(buildDir, finalName + extension);
        
    }
    
    private void generateScaContributionFile(File contentDirectory) throws TransformerException, 
                                                                           IOException, 
                                                                           ParserConfigurationException, 
                                                                           SAXException, 
                                                                           MojoExecutionException {
        
        File scaContribution = new File(contentDirectory, "META-INF" + File.separator + "sca-contribution.xml");
        
        if ((deployables != null || mavenImports != null) && scaContribution.exists()) {
            throw new MojoExecutionException("SCA contribution xml already exists");
        }
        Document document = DBF.newDocumentBuilder().newDocument();
        
        Element root = document.createElement("contribution");
        root.setAttribute("xmlns", "http://www.osoa.org/xmlns/sca/1.0");
        root.setAttribute("xmlns:maven", "http://fabric3.org/xmlns/sca/2.0-alpha/maven");
        
        document.appendChild(root);
        
        generateDeployables(contentDirectory, document, root);
        
        generateMavenImports(document, root);
        
        Transformer transformer = TF.newTransformer();
        transformer.setOutputProperty("indent", "yes");
        FileOutputStream out = new FileOutputStream(scaContribution);
        transformer.transform(new DOMSource(document), new StreamResult(out));
        
        out.close();
        
    }

    private void generateMavenImports(Document document, Element root) {
        
        if (mavenImports == null) {
            return;
        }
            
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = (Set<Artifact>) project.getArtifacts();
        
        for (MavenImport mavenImport : mavenImports) {
            
            String groupId = mavenImport.getGroupId();
            String artifactId = mavenImport.getArtifactId();
            
            Element mavenImportElement = document.createElement("maven:import");
            mavenImportElement.setAttribute("groupId",groupId);
            mavenImportElement.setAttribute("artifactId", artifactId);
            
            for (Artifact artifact : artifacts) {
                if (groupId.equals(artifact.getGroupId()) && artifactId.equals(artifact.getArtifactId())) {
                    getLog().info("Found artifact:" + artifact.getArtifactId());
                    mavenImportElement.setAttribute("version", artifact.getVersion());
                }
            }
            
            root.appendChild(mavenImportElement);
            
        }
        
    }

    private void generateDeployables(File contentDirectory, Document document, Element root) throws SAXException,
                                                                                                    IOException, 
                                                                                                    ParserConfigurationException {
        
        if (deployables == null) {
            return;
        }
            
        for (String deployable : deployables) {
            
            File deployableFile = new File(contentDirectory, deployable);
            Document composite = DBF.newDocumentBuilder().parse(deployableFile);
            Element compositeElement = composite.getDocumentElement();
            
            String uri = compositeElement.getAttribute("targetNamespace");
            String name = compositeElement.getAttribute("name");
            
            Element deployableElement = document.createElement("deployable");
            deployableElement.setAttribute("xmlns:dep", uri);
            deployableElement.setAttribute("composite", "dep:" + name);
            root.appendChild(deployableElement);
            
        }

    }

    private void includeDependencies(File contentDirectory) throws IOException {
        
	 	getLog().debug( "including dependencies in archive");
	 	//include all the dependencies that are required for runtime operation and are not sca-contributions(they
	 	// will be deployed separately);
        File libDir = new File( contentDirectory,"META-INF" + File.separator + "lib" );
	 	ScopeArtifactFilter filter = new ScopeArtifactFilter( Artifact.SCOPE_RUNTIME );

        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = (Set<Artifact>) project.getArtifacts();
        for (Artifact artifact : artifacts) {
        	getLog().debug("checking " + artifact.getArtifactId());
        	boolean isSCAContribution = artifact.getType().startsWith("sca-contribution");
            if ( !isSCAContribution && !artifact.isOptional() && filter.include( artifact ) ) {
            	getLog().debug( String.format("including dependency %s", artifact));
            	File destinationFile = new File( libDir, artifact.getFile().getName());
                if (!libDir.exists()){
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