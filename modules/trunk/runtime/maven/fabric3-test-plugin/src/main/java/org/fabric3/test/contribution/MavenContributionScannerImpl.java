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

import java.util.Set;

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
    public Set<Contribution> scan(MavenProject mavenProject) {
        
        // 1. Get all of projects dependencies
        // 2. Get all the transitive dependencies for the above
        // 3. Get the URL for all the above
        // 4. Get the URL for target/classes
        // 5. Get the URL for target/test-classes
        // 6. Create a URL classloader from 4, 5 and 6
        // 7. Call getResources("META-INF/sca-contribution.xml") on 6
        // 8. On returned URLs, compute the containing URLs
        // 9. Read the SCA contribution XML to identify a contribution as extension or not
        // 10. Check if the URL is test-classes to identify the contribution as test or not
        // 11. Return the set of contributions
        
        return null;
        
    }

}
