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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.services.contribution.ArtifactResolver;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * Default implementation of the artifact resolver registry
 *
 * @version $Rev$ $Date$
 */
public class ArtifactResolverRegistryImpl implements ArtifactResolverRegistry {
    private Map<String, ArtifactResolver> resolvers = new HashMap<String, ArtifactResolver>();

    public void register(String scheme, ArtifactResolver resolver) {
        resolvers.put(scheme, resolver);
    }

    public void unregister(String scheme) {
        resolvers.remove(scheme);
    }

    public URL resolve(URL url) throws ResolutionException {
        String scheme = url.getProtocol();
        ArtifactResolver resolver = resolvers.get(scheme);
        if (resolver == null) {
            throw new ArtifactResolverNotFoundException("Resolver not found for scheme", scheme);
        }
        return resolver.resolve(url);
    }
}
