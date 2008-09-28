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
package org.fabric3.spi.services.archive;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Implementations store artifacts
 */
public interface ArchiveStore {

    /**
     * Copies an artifact to the store.
     *
     * @param uri    A URI pointing to the artifact being copied to the store
     * @param stream InputStream with the content of the distribution
     * @return a URL pointing to the stored artifact
     * @throws ArchiveStoreException if an error occurs storing the artifact
     */
    URL store(URI uri, InputStream stream) throws ArchiveStoreException;

    /**
     * Copy an artifact from the source URL to the store
     *
     * @param contributionUri the URI of the artifact
     * @param sourceURL       the source URL of the artifact
     * @return a URL pointing to the stored artifact
     * @throws ArchiveStoreException if an error occurs storing the artifact
     */
    URL store(URI contributionUri, URL sourceURL) throws ArchiveStoreException;

    /**
     * Look up the artifact URL by URI
     *
     * @param uri The URI of the artifact
     * @return A URL pointing to the artifact or null if the artifact cannot be found
     * @throws ArchiveStoreException if an exception occurs storing the contribution
     */
    URL find(URI uri) throws ArchiveStoreException;

    /**
     * Remove an artifact from the store
     *
     * @param uri The URI of the contribution to be removed
     * @throws ArchiveStoreException if an exception occurs removing the contribution
     */
    void remove(URI uri) throws ArchiveStoreException;

    /**
     * Get list of URIs for all the artifacts in the store
     *
     * @return A list of artifact URIs
     */
    List<URI> list();
}
