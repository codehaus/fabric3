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
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.UnsupportedContentTypeException;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.model.type.ContributionResourceDescription;
import org.fabric3.spi.services.contribution.Action;
import org.fabric3.spi.services.contribution.ArchiveContributionHandler;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Resource;

/**
 * Handles common processing for contribution archives
 *
 * @version $Rev$ $Date$
 */
public class ArchiveContributionProcessor extends AbstractContributionProcessor {
    private static final String[] CONTENT_TYPES = new String[]{Constants.ZIP_CONTENT_TYPE, "application/octet-stream"};
    private ArtifactLocationEncoder encoder;
    private List<ArchiveContributionHandler> handlers;

    public ArchiveContributionProcessor(@Reference ArtifactLocationEncoder encoder) {
        this.encoder = encoder;
    }

    @Reference
    public void setHandlers(List<ArchiveContributionHandler> handlers) {
        this.handlers = handlers;
    }

    public String[] getContentTypes() {
        return CONTENT_TYPES;
    }

    public void processManifest(Contribution contribution) throws ContributionException {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.processManifest(contribution);
    }

    public void index(Contribution contribution) throws ContributionException {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.iterateArtifacts(contribution, new Action() {
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

    private ArchiveContributionHandler getHandler(Contribution contribution) throws UnsupportedContentTypeException {
        for (ArchiveContributionHandler handler : handlers) {
            if (handler.canProcess(contribution)) {
                return handler;
            }
        }
        String source = contribution.getUri().toString();
        throw new UnsupportedContentTypeException("Contribution type not supported: " + source, source);
    }

}
