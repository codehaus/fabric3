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
 *
 */
package org.fabric3.runtime.standalone.server;

import java.util.ArrayList;
import java.util.List;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Holder for extension and user contributions in the runtime repository.
 *
 * @version $Revision$ $Date$
 */
public class ScanResult {
    private List<ContributionSource> extensionContributions = new ArrayList<ContributionSource>();
    private List<ContributionSource> userContributions = new ArrayList<ContributionSource>();

    public void addExtensionContribution(ContributionSource source) {
        extensionContributions.add(source);
    }

    public void addUserContribution(ContributionSource source) {
        userContributions.add(source);
    }

    public List<ContributionSource> getExtensionContributions() {
        return extensionContributions;
    }

    public List<ContributionSource> getUserContributions() {
        return userContributions;
    }
}
