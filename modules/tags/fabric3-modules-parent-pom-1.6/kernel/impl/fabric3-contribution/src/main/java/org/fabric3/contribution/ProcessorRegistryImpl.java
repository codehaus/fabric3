/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.contribution;

import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.host.contribution.InstallException;
import org.fabric3.host.stream.Source;
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
public class ProcessorRegistryImpl implements ProcessorRegistry {
    private Map<String, ContributionProcessor> contributionProcessorCache = new HashMap<String, ContributionProcessor>();
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

    public void indexResource(Contribution contribution, String contentType, Source source, IntrospectionContext context) throws InstallException {
        ResourceProcessor processor = resourceProcessorCache.get(contentType);
        if (processor == null) {
            // unknown type, skip
            return;
        }
        processor.index(contribution, source, context);
    }

    public void processContribution(Contribution contribution, IntrospectionContext context) throws InstallException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + "in contribution " + source + " not supported", contentType);
        }
        processor.process(contribution, context);
    }

    public void processResource(Resource resource, IntrospectionContext context) throws InstallException {
        ResourceProcessor processor = resourceProcessorCache.get(resource.getContentType());
        if (processor == null) {
            return;
        }
        processor.process(resource, context);
    }

}
