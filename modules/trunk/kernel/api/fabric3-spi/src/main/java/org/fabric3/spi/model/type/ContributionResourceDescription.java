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
package org.fabric3.spi.model.type;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fabric3.scdl.ResourceDescription;

/**
 * Denotes a requirement on a contribution artifact.
 *
 * @version $Rev$ $Date$
 */
public class ContributionResourceDescription extends ResourceDescription<URI> {
    private List<URL> artifactUrls = new ArrayList<URL>();
    private List<URI> importedUris = new ArrayList<URI>();

    public ContributionResourceDescription(URI identifier) {
        super(identifier);
    }

    public ContributionResourceDescription(URI identifier, String version) {
        super(identifier, version);
    }

    public void addArtifactUrl(URL url) {
        artifactUrls.add(url);
    }

    public List<URL> getArtifactUrls() {
        return Collections.unmodifiableList(artifactUrls);
    }

    public void addImportedUri(URI uri) {
        importedUris.add(uri);
    }

    public List<URI> getImportedUris() {
        return Collections.unmodifiableList(importedUris);
    }

}
