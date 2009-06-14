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

import org.fabric3.host.Namespaces;

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
            
            URL classesDir = new File(mavenProject.getBasedir(), "target/classes").toURL();
            URL testClassesDir = new File(mavenProject.getBasedir(), "target/test-classes").toURL();
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
        
        String extension = document.getDocumentElement().getAttributeNS(Namespaces.CORE, "extension");
        return extension != null && !"".equals(extension.trim());
        
    }
    
    /*
     * Computes the contribution URL from the manifest URL.
     */
    private URL getContributionUrl(URL manifestUrl) throws MalformedURLException {
        
        String externalForm = manifestUrl.toExternalForm();
        String protocol = manifestUrl.getProtocol();
        String url;
        
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
