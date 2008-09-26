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
package org.fabric3.fabric.services.contribution.processor;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.contribution.UnsupportedContentTypeException;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Action;
import org.fabric3.spi.services.contribution.ArchiveContributionHandler;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.scdl.ValidationContext;

/**
 * Handles common processing for contribution archives
 *
 * @version $Rev$ $Date$
 */
public class ArchiveContributionProcessor extends AbstractContributionProcessor {

    private static final List<String> CONTENT_TYPES = initializeContentTypes();
    private List<ArchiveContributionHandler> handlers;

    @Reference
    public void setHandlers(List<ArchiveContributionHandler> handlers) {
        this.handlers = handlers;
        int size = handlers.size();
        for (int i = 0; i < size; i++) {
            CONTENT_TYPES.add(handlers.get(i).getContentType());
        }
    }

    public List<String> getContentTypes() {
        return CONTENT_TYPES;
    }

    public void processManifest(Contribution contribution, ValidationContext context) throws ContributionException {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.processManifest(contribution, context);
    }

    public void index(Contribution contribution, final ValidationContext context) throws ContributionException {
        ArchiveContributionHandler handler = getHandler(contribution);
        handler.iterateArtifacts(contribution, new Action() {
            public void process(Contribution contribution, String contentType, URL url)
                    throws ContributionException {
                registry.indexResource(contribution, contentType, url, context);
            }
        });

    }

    public void process(Contribution contribution, ValidationContext context, ClassLoader loader) throws ContributionException {
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        URI contributionUri = contribution.getUri();
        try {
            Thread.currentThread().setContextClassLoader(loader);
            for (Resource resource : contribution.getResources()) {
                if (!resource.isProcessed()) {
                    registry.processResource(contributionUri, resource, context, loader);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
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


    private static List<String> initializeContentTypes() {
        List<String> list = new ArrayList<String>();
        list.add("application/octet-stream");
        return list;
    }
}
