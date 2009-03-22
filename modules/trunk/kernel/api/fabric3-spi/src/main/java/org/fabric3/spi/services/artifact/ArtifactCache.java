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
package org.fabric3.spi.services.artifact;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

/**
 * Temporarily stores artifacts locally to a runtime, maintaining an in-use count.
 *
 * @version $Revision$ $Date$
 */
public interface ArtifactCache {

    /**
     * Temporarily persists an artifact. The lifetime of the artifact will not extend past the lifetime of the runtime instance. When the operation
     * completes, the input stream will be closed.
     *
     * @param uri    The artifact URI
     * @param stream the artifact contents
     * @return a URL for the persisted artifact
     * @throws CacheException if an error occurs persisting the artifact
     */
    URL cache(URI uri, InputStream stream) throws CacheException;

    /**
     * Returns the URL for the cached artifact or null if not found.
     *
     * @param uri the artifact URI
     * @return the URL for the cached artifact or null if not found
     */
    URL get(URI uri);

    /**
     * Increment the in-use count of an artifact.
     *
     * @param uri the artifact URI.
     */
    void increment(URI uri);

    /**
     * Release an artifact. If the in-use count is 1, the artifact can be evicted from local storage. If the in-use count is greater than 1, the count
     * will be decremented.
     *
     * @param uri the artifact URI.
     * @throws CacheException if an error occurs releasing the artifact
     */
    void release(URI uri) throws CacheException;
}
