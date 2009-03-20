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
package org.fabric3.provisioning.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.services.repository.Repository;
import org.fabric3.spi.services.repository.RepositoryException;

/**
 * Resolves contributions using the <code>http</code> scheme, copying them to a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpContributionUriResolver implements ContributionUriResolver {
    private static final String HTTP_SCHEME = "http";
    // the path mapping of the ArchiveResolverServlet

    private Repository repository;
    private MetaDataStore metaDataStore;
    private ArtifactResolverMonitor monitor;
    private Map<URI, Integer> counter;

    public HttpContributionUriResolver(@Reference Repository repository,
                                       @Reference MetaDataStore metaDataStore,
                                       @Monitor ArtifactResolverMonitor monitor) {
        this.repository = repository;
        this.metaDataStore = metaDataStore;
        this.monitor = monitor;
        counter = new HashMap<URI, Integer>();
    }

    public URL resolve(URI uri) throws ResolutionException {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally, resolve it directly
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                String id = uri.toString();
                throw new ResolutionException("Contribution not found: " + id, id);
            }
            return contribution.getLocation();
        }
        InputStream stream = null;
        try {
            URI decoded = URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() + 2)); // +2 for leading and trailing '/'
            // check to see if the archive is cached locally
            URL localURL = repository.find(decoded);
            if (localURL == null) {
                // resolve remotely
                URL url = uri.toURL();
                stream = url.openStream();
                localURL = repository.cache(decoded, stream);

                // update the reference count
                Integer count = counter.get(decoded);
                if (count == null) {
                    counter.put(decoded, 1);
                } else {
                    counter.put(decoded, count + 1);
                }

            }
            return localURL;
        } catch (IOException e) {
            String identifier = uri.toString();
            throw new ResolutionException("Error resolving artifact: " + identifier, identifier, e);
        } catch (RepositoryException e) {
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

    public void release(URI uri) throws ResolutionException {
        Integer count = counter.get(uri);
        if (count == null) {
            // locally provisioned
            return;
        }
        if (count == 1) {
            counter.remove(uri);
            try {
                repository.remove(uri);
            } catch (RepositoryException e) {
                throw new ResolutionException("Error removing contribution: " + uri, e);
            }
        } else {
            counter.put(uri, count - 1);
        }
    }
}
