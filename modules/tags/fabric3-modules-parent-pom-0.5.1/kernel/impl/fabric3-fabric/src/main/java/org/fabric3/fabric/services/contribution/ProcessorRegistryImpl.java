/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ManifestProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceProcessor;
import org.fabric3.scdl.ValidationContext;

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
    private Map<String, ManifestProcessor> manifestProcessorCache = new HashMap<String, ManifestProcessor>();

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

    public void register(ManifestProcessor processor) {
        manifestProcessorCache.put(processor.getContentType(), processor);
    }

    public void unregisterManifestProcessor(String contentType) {
        manifestProcessorCache.remove(contentType);
    }

    public void processManifest(Contribution contribution, ValidationContext context) throws ContributionException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + " in contribution " + source + " not supported", contentType);
        }
        processor.processManifest(contribution, context);

    }

    public void processManifestArtifact(ContributionManifest manifest, String contentType, InputStream inputStream, ValidationContext context)
            throws ContributionException {
        ManifestProcessor processor = manifestProcessorCache.get(contentType);
        if (processor != null) {
            processor.process(manifest, inputStream, context);
        }
    }

    public void indexContribution(Contribution contribution, ValidationContext context) throws ContributionException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + "in contribution " + source + " not supported", contentType);
        }
        processor.index(contribution, context);
    }

    public void indexResource(Contribution contribution, String contentType, URL url, ValidationContext context) throws ContributionException {
        ResourceProcessor processor = resourceProcessorCache.get(contentType);
        if (processor == null) {
            // unknown type, skip
            return;
        }
        processor.index(contribution, url, context);
    }

    public void processContribution(Contribution contribution, ValidationContext context, ClassLoader loader) throws ContributionException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException("Type " + contentType + "in contribution " + source + " not supported", contentType);
        }
        processor.process(contribution, context, loader);
    }

    public void processResource(URI contributionUri, Resource resource, ValidationContext context, ClassLoader loader) throws ContributionException {
        ResourceProcessor processor = resourceProcessorCache.get(resource.getContentType());
        if (processor == null) {
            // FIXME for now, return null
            return;
            //throw new UnsupportedContentTypeException(contentType);
        }
        processor.process(contributionUri, resource, context, loader);
    }

}
