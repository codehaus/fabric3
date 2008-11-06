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
package org.fabric3.spi.services.contribution;

/**
 * Implementations receive callbacks for ContributionService events.
 *
 * @version $Revision$ $Date$
 */
public interface ContributionServiceListener {

    /**
     * Called when a contribution is stored.
     *
     * @param contribution the contribution
     */
    void onStore(Contribution contribution);

    /**
     * Called when a contribution is installed.
     *
     * @param contribution the contribution
     */
    void onInstall(Contribution contribution);

    /**
     * Called when a contribution is updated.
     *
     * @param contribution the contribution
     */
    void onUpdate(Contribution contribution);

    /**
     * Called when a contribution is uninstalled.
     *
     * @param contribution the contribution
     */
    void onUninstall(Contribution contribution);

    /**
     * Called when a contribution is removed.
     *
     * @param contribution the contribution
     */
    void onRemove(Contribution contribution);
}
