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

import java.net.URI;
import java.util.Set;

import org.fabric3.host.contribution.ContributionException;

/**
 * Thrown when there is an attempt to unload a contribution referenced by other installed contributions.
 *
 * @version $Revision$ $Date$
 */
public class ContributionInUseException extends ContributionException {
    private static final long serialVersionUID = 3826037592455762508L;
    private Set<URI> contributions;
    private URI uri;

    /**
     * Constructor.
     *
     * @param message       the error message
     * @param uri           the contribution
     * @param contributions the installed contributions that reference the contribution
     */
    public ContributionInUseException(String message, URI uri, Set<URI> contributions) {
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
