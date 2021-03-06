/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.host.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Represents a source artifact that will be contributed to a domain or an updated version of an existing contribution.
 *
 * @version $Rev$ $Date$
 */
public interface ContributionSource {

    /**
     * Returns true if the source shoud be persisted.
     *
     * @return true if the source shoud be persisted
     */
    boolean persist();

    /**
     * Returns the identifier for this contribution or null if one has not been assigned (i.e. it is a new contribution
     * and not an update).
     *
     * @return the identifier for this contribution
     */
    URI getUri();

    /**
     * Returns a input stream for the source.
     *
     * @return a input stream for the source
     * @throws IOException if an error occurs returning the stream
     */
    InputStream getSource() throws IOException;

    /**
     * If the source is local, returns the source URL
     *
     * @return the source URL
     */
    URL getLocation();

    /**
     * Returns the source timestamp.
     *
     * @return the source timestamp
     */
    long getTimestamp();

    /**
     * Returns the source checksum.
     *
     * @return the source checksum
     */
    byte[] getChecksum();


  /**
   * Returns the content type of the source 
   * @return
   */
    String getContentType();
}