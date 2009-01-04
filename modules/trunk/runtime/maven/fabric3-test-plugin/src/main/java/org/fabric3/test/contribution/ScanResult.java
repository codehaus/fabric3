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

import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * Result of a contribution scan.
 *
 */
public class ScanResult {
    
    private URL testContribution;
    private Set<URL> userContributions = new HashSet<URL>();
    private Set<URL> extensionContributions = new HashSet<URL>();

    /**
     * Initializes the test contribution.
     * 
     * @param testContribution Test contribution.
     */
    public ScanResult(URL testContribution) {
        this.testContribution = testContribution;
    }
    
    /**
     * Gets the test contribution.
     * 
     * @return Test contribution.
     */
    public URL getTestContribution() {
        return testContribution;
    }
    
    /**
     * Gets the user contributions.
     * 
     * @return User contributions.
     */
    public Set<URL> getUserContributions() {
        return userContributions;
    }
    
    /**
     * Adds a user contribution.
     * 
     * @param userContribution User contribution.
     */
    public void addUserContribution(URL userContribution) {
        userContributions.add(userContribution);
    }
    
    /**
     * Gets the extension contributions.
     * 
     * @return Extension contributions.
     */
    public Set<URL> getExtensionContributions() {
        return extensionContributions;
    }
    
    /**
     * Adds an extension contribution.
     * 
     * @param extensionContribution Extension contribution.
     */
    public void addExtensionContribution(URL extensionContribution) {
        extensionContributions.add(extensionContribution);
    }

}
