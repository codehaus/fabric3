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
package org.fabric3.spi.contribution.archive;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.model.type.ValidationContext;
import org.fabric3.spi.contribution.Contribution;

/**
 * Responsible for handling the specifics of an archive-based contribution.
 *
 * @version $Revision$ $Date$
 */
public interface ArchiveContributionHandler {

    /**
     * Returns the content type of the contribution that this handler can process
     *
     * @return the content type.
     */

    String getContentType();

    /**
     * Returns true if the implementation can process the contribution archive.
     *
     * @param contribution the contribution
     * @return true if the implementation can process the contribution archive
     */
    boolean canProcess(Contribution contribution);

    /**
     * Processes the manifest
     *
     * @param contribution the contribution
     * @param context      the context to which validation errors and warnings are reported
     * @throws InstallException if an error occurs processing the manifest
     */
    void processManifest(Contribution contribution, ValidationContext context) throws InstallException;

    /**
     * Iterates through a contribution calling the supplied action when a contained artifact is encountered.
     *
     * @param contribution the contribution
     * @param action       the action to perform when an artifact is encountered
     * @throws InstallException if an error occurs processing the manifest
     */
    void iterateArtifacts(Contribution contribution, Action action) throws InstallException;

}
