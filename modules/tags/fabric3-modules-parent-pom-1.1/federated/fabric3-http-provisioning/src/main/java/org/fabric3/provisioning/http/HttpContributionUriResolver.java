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

import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionUriResolver;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.ResolutionException;
import org.fabric3.spi.services.artifact.ArtifactCache;
import org.fabric3.spi.services.artifact.CacheException;

/**
 * Resolves contributions using the <code>http</code> scheme, copying them to a local archive store.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class HttpContributionUriResolver implements ContributionUriResolver {
    private static final String HTTP_SCHEME = "http";

    private ArtifactCache cache;
    private MetaDataStore metaDataStore;

    public HttpContributionUriResolver(@Reference ArtifactCache cache, @Reference MetaDataStore store) {
        this.cache = cache;
        this.metaDataStore = store;
    }

    public URI decode(URI uri) {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally
            return uri;
        }
        return URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() + 2)); // +2 for leading and trailing '/'
    }

    public URL resolve(URI uri) throws ResolutionException {
        if (!HTTP_SCHEME.equals(uri.getScheme())) {
            // the contribution is being provisioned locally, resolve it directly
            Contribution contribution = metaDataStore.find(uri);
            if (contribution == null) {
                throw new ResolutionException("Contribution not found: " + uri);
            }
            return contribution.getLocation();
        }
        InputStream stream;
        try {
            URI decoded = URI.create(uri.getPath().substring(HttpProvisionConstants.REPOSITORY.length() + 2)); // +2 for leading and trailing '/'
            // check to see if the archive is cached locally
            URL localURL = cache.get(decoded);
            if (localURL == null) {
                // resolve remotely
                URL url = uri.toURL();
                stream = url.openStream();
                localURL = cache.cache(decoded, stream);
            }
            return localURL;
        } catch (IOException e) {
            throw new ResolutionException("Error resolving artifact: " + uri, e);
        } catch (CacheException e) {
            throw new ResolutionException("Error resolving artifact: " + uri, e);
        }
    }

    public void release(URI uri) throws ResolutionException {
        try {
            cache.release(uri);
        } catch (CacheException e) {
            throw new ResolutionException("Error releasing artifact: " + uri, e);
        }
    }

    public int getInUseCount(URI uri) {
        return cache.getCount(uri);
    }
}

