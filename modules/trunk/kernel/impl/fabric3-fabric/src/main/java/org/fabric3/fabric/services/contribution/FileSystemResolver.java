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

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.services.contribution.ArtifactResolver;
import org.fabric3.spi.services.contribution.ArtifactResolverRegistry;
import org.fabric3.spi.services.contribution.ResolutionException;

/**
 * Resolves artifacts for the <code>file://</code> scheme. Since a file system is accessible as a "local" resource, this
 * implementation simply returns the artifact URL.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class FileSystemResolver implements ArtifactResolver {
    public static final String FILE_SCHEME = "file";

    private ArtifactResolverRegistry registry;

    public FileSystemResolver(@Reference ArtifactResolverRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(FILE_SCHEME, this);
    }

    public URL resolve(URL url) throws ResolutionException {
        return url;
    }
}
