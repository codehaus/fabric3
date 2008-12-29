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
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ContributionProcessor;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.introspection.IntrospectionContext;

/**
 * Default implementation of ProcessorRegistry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ProcessorRegistry.class)
public class ProcessorRegistryImpl implements ProcessorRegistry {
    private Map<String, ContributionProcessor> contributionProcessorCache =
            new HashMap<String, ContributionProcessor>();
    private Map<String, ResourceProcessor> resourceProcessorCache = new HashMap<String, ResourceProcessor>();

    public ProcessorRegistryImpl() {
    }

    public void register(ContributionProcessor processor) {
        for (String contentType : processor.getContentTypes()) {
            contributionProcessorCache.put(contentType, processor);
        }
    }

    public void unregisterContributionProcessor(String contentType) {
        contributionProcessorCache.remove(contentType);
    }

    public void register(ResourceProcessor processor) {
        resourceProcessorCache.put(processor.getContentType(), processor);
    }

    public void unregisterResourceProcessor(String contentType) {
        resourceProcessorCache.remove(contentType);
    }

    public void processManifest(Contribution contribution, IntrospectionContext context) throws InstallException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + " in contribution " + source + " not supported", contentType);
        }
        processor.processManifest(contribution, context);

    }

    public void indexContribution(Contribution contribution, IntrospectionContext context) throws InstallException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + "in contribution " + source + " not supported", contentType);
        }
        processor.index(contribution, context);
    }

    public void indexResource(Contribution contribution, String contentType, URL url, IntrospectionContext context) throws InstallException {
        ResourceProcessor processor = resourceProcessorCache.get(contentType);
        if (processor == null) {
            // unknown type, skip
            return;
        }
        processor.index(contribution, url, context);
    }

    public void processContribution(Contribution contribution, IntrospectionContext context, ClassLoader loader) throws InstallException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + "in contribution " + source + " not supported", contentType);
        }
        processor.process(contribution, context, loader);
    }

    public void processResource(URI contributionUri, Resource resource, IntrospectionContext context, ClassLoader loader) throws InstallException {
        ResourceProcessor processor = resourceProcessorCache.get(resource.getContentType());
        if (processor == null) {
            // FIXME for now, return null
            return;
            //throw new UnsupportedContentTypeException(contentType);
        }
        processor.process(contributionUri, resource, context, loader);
    }

}
