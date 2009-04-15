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
package org.fabric3.spi.contribution;

import java.net.URI;
import java.net.URL;


/**
 * Implementations resolve contribution artifacts in a domain to a local cache
 *
 * @version $Rev$ $Date$
 */
public interface ContributionUriResolver {

    /**
     * Decodes a URI.
     *
     * @param uri the uri
     * @return the decoded uri
     */
    public URI decode(URI uri);

    /**
     * Resolves the contribution artifact associated with the URI, returning a local URL by which it may be dereferenced. Resolving the artifact also
     * increments the in-use count.
     *
     * @param contributionURI the contribution URI
     * @return the local dereferenceable URL for the artifact
     * @throws ResolutionException if an error occurs resolving the artifact
     */
    URL resolve(URI contributionURI) throws ResolutionException;

    /**
     * Releases a previously resolved contribution. If the in-use count reaches 0, the artifact will be evicted.
     *
     * @param uri the contribution  URI.
     * @throws ResolutionException if an error occurs releasing the artifact
     */
    void release(URI uri) throws ResolutionException;

    /**
     * Returns the in-use count if the resolver does not support caching.
     *
     * @param uri the artifact
     * @return the in-use count
     */
    int getInUseCount(URI uri);

}
