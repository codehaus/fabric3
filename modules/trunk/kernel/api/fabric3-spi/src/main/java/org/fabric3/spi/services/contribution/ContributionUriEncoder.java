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

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Implementations encode a contribution URI so that it may be dereferenced remotely in a domain, e.g. over HTTP.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionUriEncoder {

    /**
     * Encode the local contribution URI.
     *
     * @param uri the uri to encode
     * @return the encoded URI which may be dereferenced in the domain
     * @throws URISyntaxException if the URI is invalid
     */
    URI encode(URI uri) throws URISyntaxException;

}
