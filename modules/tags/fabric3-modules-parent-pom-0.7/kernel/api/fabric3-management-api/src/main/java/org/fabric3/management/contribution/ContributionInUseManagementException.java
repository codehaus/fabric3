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
package org.fabric3.management.contribution;

import java.net.URI;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public class ContributionInUseManagementException extends ContributionUninstallException {
    private static final long serialVersionUID = -4801764591014282993L;
    private Set<URI> contributions;
    private URI uri;

    /**
     * Constructor.
     *
     * @param message       the error message
     * @param uri           the contribution
     * @param contributions the installed contributions that reference the contribution
     */
    public ContributionInUseManagementException(String message, URI uri, Set<URI> contributions) {
        super(message);
        this.uri = uri;
        this.contributions = contributions;
    }

    public Set<URI> getContributions() {
        return contributions;
    }

    public URI getUri() {
        return uri;
    }
}
