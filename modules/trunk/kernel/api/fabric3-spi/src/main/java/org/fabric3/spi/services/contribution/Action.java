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

import java.net.URL;

import org.fabric3.host.contribution.InstallException;

/**
 * Used to perform a callback operation when iterating contained artifacts in a contribution.
 *
 * @version $Rev$ $Date$
 */
public interface Action {
    /**
     * Called when an artifact is reached during iteration.
     *
     * @param contribution the contribution being traversed
     * @param contentType  the artifact MIME type to process
     * @param url          the artifact url
     * @throws InstallException if an error occurs processing the artifact
     */
    void process(Contribution contribution, String contentType, URL url) throws InstallException;
}
