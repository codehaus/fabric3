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

import java.io.IOException;
import java.net.MalformedURLException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Scans for candidate contribution. A contribution is a location JAR or 
 * file-system directory that contains a META-INF/sca-contribution.xml.
 *
 */
public interface MavenContributionScanner {
    
    /**
     * Scans for possible contributions in the project.
     * 
     * @param mavenProject Maven project.
     * @return Returns a set of identified contributions.
     */
    ScanResult scan(MavenProject mavenProject) throws MojoExecutionException, IOException;

}
