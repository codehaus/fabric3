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
 */
package org.fabric3.test.contribution;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Default implementation of the Maven contribution scanner.
 *
 */
public class MavenContributionScannerImpl implements MavenContributionScanner {
    
    private static final String MANIFEST_PATH = "META-INF/sca-contribution.xml";
    
    /**
     * Scans for possible contributions in the project.
     * 
     * @param mavenProject Maven project.
     * @return Returns a set of identified contributions.
     */
    public ScanResult scan(MavenProject mavenProject) throws MojoExecutionException {
        
        try {
            
            ScanResult scanResult = new ScanResult();
            
            Set<?> artifacts = mavenProject.getArtifacts();
            
            URL classesDir = new File("target/classes").toURL();
            URL testClassesDir = new File("target/test-classes").toURL();
            URL[] urls = new URL[artifacts.size() + 2];
            
            urls[0] = classesDir;
            urls[1] = testClassesDir;
            Iterator<?> iterator = artifacts.iterator();
            
            for (int i = 2;i < urls.length;i++) {
                URL artifactUrl = Artifact.class.cast(iterator.next()).getFile().toURL();
                urls[i] = artifactUrl;
            }
            
            URLClassLoader urlClassLoader = new URLClassLoader(urls);
            Enumeration<URL> scannedManifests = urlClassLoader.getResources(MANIFEST_PATH);
            
            while (scannedManifests.hasMoreElements()) {
                URL manifestUrl = scannedManifests.nextElement();
                URL contributionUrl = getContributionUrl(manifestUrl);
                boolean extension = isExtension(manifestUrl);
                scanResult.addContribution(contributionUrl, extension);
            }
            
            return scanResult;
            
        } catch (IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (ParserConfigurationException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        } catch (SAXException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
        
    }
    
    /*
     * Checks whether the manifest is an extension.
     */
    private boolean isExtension(URL manifestUrl) throws ParserConfigurationException, IOException, SAXException {
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        
        InputStream stream = manifestUrl.openStream();
        Document document = db.parse(stream);
        stream.close();
        
        String extension = document.getDocumentElement().getAttributeNS("urn:fabric3.org:core", "extension");
        return extension != null && !"".equals(extension.trim());
        
    }
    
    /*
     * Computes the contribution URL from the manifest URL.
     */
    private URL getContributionUrl(URL manifestUrl) throws MalformedURLException {
        
        String externalForm = manifestUrl.toExternalForm();
        String protocol = manifestUrl.getProtocol();
        String url = null;
        
        if ("jar".equals(protocol)) {
            url = externalForm.substring(0, externalForm.indexOf("!/" + MANIFEST_PATH));
            // Strip the jar protocol
            url = url.substring(4);
        } else {
            url = externalForm.substring(0, externalForm.indexOf(MANIFEST_PATH));
        }
        return new URL(url);
        
    }

}
