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
package org.fabric3.fabric.services.contribution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.ArtifactResolver;
import org.fabric3.spi.services.contribution.ArtifactResolverMonitor;
import org.fabric3.spi.services.contribution.ResolutionException;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.Contribution;

/**
 * Resolves artifacts for the <code>http://</code> scheme, storing them in a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpResolver implements ArtifactResolver {
    private static final String HTTP_SCHEME = "http";

    private ArchiveStore archiveStore;
    private MetaDataStore metaDataStore;
    private ArtifactResolverMonitor monitor;

    public HttpResolver(@Reference ArchiveStore archiveStore, @Reference MetaDataStore metaDataStore, @Monitor ArtifactResolverMonitor monitor) {
        this.archiveStore = archiveStore;
        this.metaDataStore = metaDataStore;
        this.monitor = monitor;
    }

    public URL resolve(URI uri) throws ResolutionException {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                String id = uri.toString();
                throw new ResolutionException("Contribution not fould: " + id, id);
            }
            return contribution.getLocation();
        }
        InputStream stream = null;
        try {
            // check to see if the archive is cached locally
            URL localURL = archiveStore.find(uri);
            if (localURL == null) {
                // resolve remotely
                URL url = uri.toURL();
                stream = url.openStream();
                localURL = archiveStore.store(uri, stream);
            }
            return localURL;
        } catch (IOException e) {
            String identifier = uri.toString();
            throw new ResolutionException("Error resolving artifact: " + identifier, identifier, e);
        } catch (ArchiveStoreException e) {
            String identifier = uri.toString();
            throw new ResolutionException("Error resolving artifact: " + identifier, identifier, e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                monitor.resolutionError(e);
            }
        }
    }
}
