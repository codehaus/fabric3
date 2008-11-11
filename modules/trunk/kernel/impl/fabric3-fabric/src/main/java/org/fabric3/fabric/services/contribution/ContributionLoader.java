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
package org.fabric3.fabric.services.contribution;

import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.MatchingExportNotFoundException;
import org.fabric3.host.contribution.ContributionInUseException;

/**
 * Loads an installed contribution.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionLoader {

    /**
     * Performs the load operation. This includes resolution of dependent contributions if necessary, and constructing a classloader with access to
     * resources contained in and required by the contribution.
     *
     * @param contribution the contribution to load
     * @return the classloader with access to the contribution and dependent resources
     * @throws ContributionLoadException if an error occurs during load
     * @throws MatchingExportNotFoundException
     *                                   if matching export could not be found
     */
    ClassLoader load(Contribution contribution) throws ContributionLoadException, MatchingExportNotFoundException;

    /**
     * Unloads a contribution from memory.
     *
     * @param contribution the contribution to unload
     * @throws ContributionInUseException if the contribution cannot be unloaded because it is referenced by another loaded contribution
     */
    void unload(Contribution contribution) throws ContributionInUseException;
}
