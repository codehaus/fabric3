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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.spi.services.archive.ArchiveStore;
import org.fabric3.spi.services.archive.ArchiveStoreException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionUriResolver;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * Resolves contributions using the <code>http</code> scheme, copying them to a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpContributionUriResolver implements ContributionUriResolver {
    private static final String HTTP_SCHEME = "http";
    // the path mapping of the ArchiveResolverServlet

    private ArchiveStore archiveStore;
    private MetaDataStore metaDataStore;
    private ArtifactResolverMonitor monitor;

    public HttpContributionUriResolver(@Reference ArchiveStore archiveStore,
                                       @Reference MetaDataStore metaDataStore,
                                       @Monitor ArtifactResolverMonitor monitor) {
        this.archiveStore = archiveStore;
        this.metaDataStore = metaDataStore;
        this.monitor = monitor;
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
            URI decoded = URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() +2)); // +2 to account for leading and trailing '/'
            // check to see if the archive is cached locally
            URL localURL = archiveStore.find(decoded);
            if (localURL == null) {
                // resolve remotely
                URL url = uri.toURL();
                stream = url.openStream();
                localURL = archiveStore.store(decoded, stream);
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
