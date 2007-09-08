/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
     * Returns true if the source is local.
     *
     * @return true if the source is local
     */
    boolean isLocal();

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
}