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
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceProcessor;

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

    public void processManifest(Contribution contribution) throws ContributionException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            String source = contribution.getUri().toString();
            throw new UnsupportedContentTypeException(contentType, source);
        }
        processor.processManifest(contribution);

    }

    public void processContribution(Contribution contribution, URI source) throws ContributionException {
        processContribution(contribution);
    }

    public void processContribution(Contribution contribution) throws ContributionException {
        String contentType = contribution.getContentType();
        ContributionProcessor processor = contributionProcessorCache.get(contentType);
        if (processor == null) {
            URI source = contribution.getUri();
            throw new UnsupportedContentTypeException(contentType, source.toString());
        }
        processor.processContent(contribution);
    }

    public Resource processResource(String contentType, InputStream inputStream) throws ContributionException {
        ResourceProcessor processor = resourceProcessorCache.get(contentType);
        if (processor == null) {
            // FIXME for now, return null
            return null;
            //throw new UnsupportedContentTypeException(contentType);
        }
        return processor.process(inputStream);
    }

}
