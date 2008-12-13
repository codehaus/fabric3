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
package org.fabric3.contribution;

import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionUriResolver;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * Resolves contribution URIs locally (i.e. in the same runtime VM).
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class LocalContributionUriResolver implements ContributionUriResolver {
    private MetaDataStore store;

    public LocalContributionUriResolver(@Reference MetaDataStore store) {
        this.store = store;
    }

    public URL resolve(URI uri) throws ResolutionException {
        Contribution contribution = store.find(uri);
        if (contribution == null) {
            String id = uri.toString();
            throw new ResolutionException("Contribution not found: " + id, id);
        }
        return contribution.getLocation();
    }
}
