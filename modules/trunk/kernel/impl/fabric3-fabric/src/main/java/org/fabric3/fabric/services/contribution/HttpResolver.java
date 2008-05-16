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
import java.net.URISyntaxException;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.ArtifactResolver;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ResolutionException;
import org.fabric3.spi.services.contribution.ArtifactResolverMonitor;
import org.fabric3.api.annotation.Monitor;

/**
 * Resolves artifacts for the <code>http://</code> scheme, storing them in a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpResolver implements ArtifactResolver {
    public static final String HTTP_SCHEME = "http";

    private ArtifactResolverRegistry registry;
    private ArchiveStore store;
    private ArtifactResolverMonitor monitor;

    public HttpResolver(@Reference ArtifactResolverRegistry registry,
                        @Reference(name = "localArchiveStore")ArchiveStore store,
                        @Monitor ArtifactResolverMonitor monitor) {
        this.registry = registry;
        this.store = store;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        registry.register(HTTP_SCHEME, this);
    }

    public URL resolve(URL url) throws ResolutionException {
        InputStream stream = null;
        try {
            URI uri = url.toURI();
            // check to see if the archive is cached locally
            URL localURL = store.find(uri);
            if (localURL == null) {
                // resolve remotely
                stream = url.openStream();
                localURL = store.store(url.toURI(), stream);
            }
            return localURL;
        } catch (URISyntaxException e) {
            String identifier = url.toString();
            throw new ResolutionException("URL cannot be converted to URI: " + identifier, identifier, e);
        } catch (IOException e) {
            String identifier = url.toString();
            throw new ResolutionException("Error resolving artifact: " + identifier, identifier, e);
        } catch (ArchiveStoreException e) {
            String identifier = url.toString();
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
