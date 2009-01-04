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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Default implementation of the Maven contribution scanner.
 *
 */
public class MavenContributionScannerImpl implements MavenContributionScanner {
    
    /**
     * Scans for possible contributions in the project.
     * 
     * @param mavenProject Maven project.
     * @return Returns a set of identified contributions.
     */
    public ScanResult scan(MavenProject mavenProject) throws MojoExecutionException, IOException {
        
        URL testContribution = getTestContribution(mavenProject);
        
        ScanResult scanResult = new ScanResult(testContribution);
        
        Set<?> artifacts = mavenProject.getArtifacts();
        URL classesDir = new File("target/test-classes").toURL();
        URL[] urls = new URL[artifacts.size() + 1];
        
        urls[0] = classesDir;
        Iterator<?> iterator = artifacts.iterator();
        for (int i = 1; i < urls.length;i++) {
            urls[i] = Artifact.class.cast(iterator.next()).getFile().toURL();
        }
        
        URLClassLoader urlClassLoader = new URLClassLoader(urls);
        Enumeration<URL> scannedManifests = urlClassLoader.getResources("META-INF/sca-contribution.xml");
        
        while (scannedManifests.hasMoreElements()) {
            // 1. On returned URLs, compute the containing URLs relative to META-INF/sca-contribution.xml
            // 2. Read the SCA contribution XML to identify a contribution as extension or not
            // 3. Add the user or extension contribution to the scan result
        }
        
        return scanResult;
        
    }

    /*
     * Gets the test contribution. Test contribution is the contents of target/test-classes directory 
     * with an sca-contribution.xml in the META-INF directory.
     */
    private URL getTestContribution(MavenProject mavenProject) throws MojoExecutionException, MalformedURLException {
        
        File testManifest = new File(mavenProject.getBasedir(), "target/test-classes/META-INF/sca-contribution.xml");
        if (!testManifest.exists()) {
            throw new MojoExecutionException("No sca-contribution.xml in test/resources/META-INF");
        }
        return new File(mavenProject.getBasedir(), "target/test-classes").toURL();
        
    }

}
