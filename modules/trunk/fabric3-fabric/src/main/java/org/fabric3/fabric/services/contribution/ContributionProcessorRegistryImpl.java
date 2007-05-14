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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Service;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.contribution.UnsupportedContentTypeException;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ContributionProcessorRegistry;

/**
 * Default implementation of ContributionProcessorRegistry
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ContributionProcessorRegistry.class)
public class ContributionProcessorRegistryImpl implements ContributionProcessorRegistry {
    private Map<String, ContributionProcessor> registry = new HashMap<String, ContributionProcessor>();

    public ContributionProcessorRegistryImpl() {
    }

    public void register(ContributionProcessor processor) {
        registry.put(processor.getContentType(), processor);
    }

    public void unregister(String contentType) {
        registry.remove(contentType);
    }

    public void processContent(Contribution contribution, String contentType, URI source, InputStream inputStream)
            throws ContributionException, IOException {

        URL locationURL = contribution.getLocation();
        ContributionProcessor processor = this.registry.get(contentType);
        if (processor == null) {
            throw new UnsupportedContentTypeException(contentType, locationURL.getPath());
        }

        processor.processContent(contribution, source, inputStream);

    }

}
