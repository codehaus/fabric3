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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.contribution.ContributionUriResolver;
import org.fabric3.spi.services.contribution.Contribution;
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
        if (store != null) {
            Contribution contribution = store.find(uri);
            if (contribution == null) {
                String id = uri.toString();
                throw new ResolutionException("Contribution not found: " + id, id);
            }
            return contribution.getLocation();
        }
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new ResolutionException(e);
        }
    }
}
