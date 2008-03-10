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
package org.fabric3.fabric.services.contribution.processor;

import java.net.URI;
import java.net.URL;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Handles common processing for contribution archives
 *
 * @version $Rev$ $Date$
 */
public abstract class ArchiveContributionProcessor extends AbstractContributionProcessor {
    protected ArtifactLocationEncoder encoder;

    protected ArchiveContributionProcessor(ArtifactLocationEncoder encoder) {
        this.encoder = encoder;
    }

    public void index(Contribution contribution) throws ContributionException {
        iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws ContributionException {
                registry.indexResource(contribution, contentType, url);
            }
        });

    }

    public void process(Contribution contribution, ClassLoader loader) throws ContributionException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        URI contributionUri = contribution.getUri();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (Resource resource : contribution.getResources()) {
                registry.processResource(contributionUri, resource, loader);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    public void updateContributionDescription(Contribution contribution, ContributionResourceDescription description)
            throws ContributionException {
        URL encodedLocation = encoder.encode(contribution.getLocation());
        description.addArtifactUrl(encodedLocation);
    }

    protected abstract void iterateArtifacts(Contribution contribution, Action action)
            throws ContributionException;
}
