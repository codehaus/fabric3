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

package org.fabric3.extension.contribution;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.spi.services.contribution.ContributionProcessor;
import org.fabric3.spi.services.contribution.ProcessorRegistry;

/**
 * The base class for ContributionProcessor implementations
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(ContributionProcessor.class)
public abstract class ContributionProcessorExtension implements ContributionProcessor {
    protected ProcessorRegistry registry;

    /**
     * Sets the ContributionProcessorRegistry that this processor should register with/
     *
     * @param registry the ContributionProcessorRegistry that this processor should register with
     */
    @Reference
    public void setContributionProcessorRegistry(ProcessorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Initialize the processor and registers with the contribution processor registry.
     */
    @Init
    public void start() {
        registry.register(this);
    }

    /**
     * Shuts the processor down and unregisters from the contribution processor registry.
     */
    @Destroy
    public void stop() {
        for(String contentType : getContentTypes()) {
            registry.unregisterContributionProcessor(contentType);
        }
    }

}
