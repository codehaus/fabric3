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
package org.fabric3.jpa.generator;

import java.net.URI;

import org.fabric3.jpa.PersistenceUnitResource;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.ResourceWireGenerator;
import org.fabric3.spi.model.instance.LogicalResource;
import org.osoa.sca.annotations.Reference;

/**
 *
 * @version $Revision$ $Date$
 */
public class PersistenceUnitResourceWireGenerator implements ResourceWireGenerator<PersistenceUnitWireTargetDefinition, PersistenceUnitResource> {
    
    private ClassLoaderGenerator classLoaderGenerator;

    /**
     * Injects the generator registry.
     * 
     * @param generatorRegistry Generator registry to be injected.
     */
    public PersistenceUnitResourceWireGenerator(@Reference GeneratorRegistry generatorRegistry,
                                                @Reference ClassLoaderGenerator classLoaderGenerator) {
        generatorRegistry.register(PersistenceUnitResource.class, this);
        this.classLoaderGenerator = classLoaderGenerator;
    }

    /**
     * @see org.fabric3.spi.generator.ResourceWireGenerator#genearteWireTargetDefinition(org.fabric3.spi.model.instance.LogicalResource)
     */
    public PersistenceUnitWireTargetDefinition genearteWireTargetDefinition(LogicalResource<PersistenceUnitResource> logicalResource,
                                                                            GeneratorContext context) throws GenerationException {
        
        URI classLoaderUri = classLoaderGenerator.generate(logicalResource, context);
            
        PersistenceUnitWireTargetDefinition pwtd = new PersistenceUnitWireTargetDefinition();
        pwtd.setUnitName(logicalResource.getResourceDefinition().getUnitName());
        pwtd.setClassLoaderUri(classLoaderUri);
            
        return pwtd;
        
    }

}
