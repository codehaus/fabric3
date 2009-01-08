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
import java.net.URI;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;

/**
 * Result of a contribution scan.
 *
 */
public class ScanResult {
    
    private ContributionSource testContribution;
    private List<ContributionSource> userContributions = new LinkedList<ContributionSource>();
    private List<ContributionSource> extensionContributions = new LinkedList<ContributionSource>();

    /**
     * Initializes the test contribution.
     * 
     * @param testContributionUrl Test contribution URL.
     */
    public ScanResult(URL testContributionUrl) {
        URI uri = URI.create(new File(testContributionUrl.getFile()).getName());
        testContribution = new FileContributionSource(uri, testContributionUrl, -1, null);
    }
    
    /**
     * Gets the test contribution.
     * 
     * @return Test contribution.
     */
    public ContributionSource getTestContribution() {
        return testContribution;
    }
    
    /**
     * Gets the user contributions.
     * 
     * @return User contributions.
     */
    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }
    
    /**
     * Gets the extension contributions.
     * 
     * @return Extension contributions.
     */
    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }
    
    /**
     * Adds a  contribution.
     * 
     * @param contributionUrl Contribution URL.
     * @param extension True if the contribution is an extension
     */
    public void addContribution(URL contributionUrl, boolean extension) {
        URI uri = URI.create(new File(contributionUrl.getFile()).getName());
        ContributionSource contributionSource = new FileContributionSource(uri, contributionUrl, -1, null);
        if (extension) {
            extensionContributions.add(contributionSource);
        } else {
            userContributions.add(contributionSource);
        }
    }

}
