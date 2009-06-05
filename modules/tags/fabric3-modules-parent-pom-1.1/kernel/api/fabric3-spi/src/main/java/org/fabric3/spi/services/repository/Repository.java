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
package org.fabric3.spi.services.repository;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * Implementations store and retrieve artifacts from persistent storage.
 */
public interface Repository {

    /**
     * Persists a user artifact to the repository.
     *
     * @param uri    The artifact URI
     * @param stream the artifact contents
     * @return a URL for the persisted artifact
     * @throws RepositoryException if an error occurs storing the artifact
     */
    URL store(URI uri, InputStream stream) throws RepositoryException;

    /**
     * Returns true if the artifact exists.
     *
     * @param uri the artifact URI
     * @return true if the archive exists
     */
    boolean exists(URI uri);

    /**
     * Look up the artifact URL by URI.
     *
     * @param uri The artifact URI
     * @return A URL pointing to the artifact or null if the artifact cannot be found
     * @throws RepositoryException if an exception occurs storing the artifact
     */
    URL find(URI uri) throws RepositoryException;

    /**
     * Removes an artifact from the repository.
     *
     * @param uri The URI of the artifact to be removed
     * @throws RepositoryException if an exception occurs removing the artifact
     */
    void remove(URI uri) throws RepositoryException;

    /**
     * Returns a list of URIs for all the artifacts in the repository.
     *
     * @return A list of artifact URIs
     */
    List<URI> list();
}
