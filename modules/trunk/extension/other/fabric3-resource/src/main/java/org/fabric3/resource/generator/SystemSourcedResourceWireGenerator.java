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
package org.fabric3.resource.generator;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.resource.model.SystemSourcedWireTargetDefinition;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalResource;

/**
 * @version $Revision$ $Date$
 */
@SuppressWarnings("unchecked")
@EagerInit
public class SystemSourcedResourceWireGenerator implements ResourceWireGenerator<SystemSourcedWireTargetDefinition, SystemSourcedResource> {

    private static final String SYSTEM_URI = "fabric3://./runtime/";

    private GeneratorRegistry registry;

    /**
     * @param registry Injected registry.
     */
    @Reference
    public void setRegistry(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    /**
     * Registers with the registry.
     */
    @Init
    public void start() {
        registry.register(SystemSourcedResource.class, this);
    }

    public SystemSourcedWireTargetDefinition generateWireTargetDefinition(LogicalResource<SystemSourcedResource> logicalResource,
                                                                          GeneratorContext context)
            throws GenerationException {

        SystemSourcedResource<?> resourceDefinition = logicalResource.getResourceDefinition();
        String mappedName = resourceDefinition.getMappedName();

        if (mappedName == null) {
            throw new MappedNameNotFoundException();
        }

        URI targetUri = URI.create(SYSTEM_URI + mappedName);

        SystemSourcedWireTargetDefinition wtd = new SystemSourcedWireTargetDefinition();
        wtd.setUri(targetUri);

        return wtd;

    }

}
