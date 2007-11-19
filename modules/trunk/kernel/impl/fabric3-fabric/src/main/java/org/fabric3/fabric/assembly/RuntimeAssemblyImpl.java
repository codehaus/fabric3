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
package org.fabric3.fabric.assembly;

import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.spi.assembly.AssemblyStore;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.physical.PhysicalModelGenerator;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.osoa.sca.annotations.Reference;

/**
 * The default RuntimeAssembly implementation
 *
 * @version $Rev$ $Date$
 */
public class RuntimeAssemblyImpl extends AbstractAssembly implements RuntimeAssembly {
    public RuntimeAssemblyImpl(@Reference GeneratorRegistry generatorRegistry,
                               @Reference WireResolver wireResolver,
                               @Reference PromotionNormalizer normalizer,
                               @Reference Allocator allocator,
                               @Reference RoutingService routingService,
                               @Reference AssemblyStore store,
                               @Reference MetaDataStore metaDataStore,
                               @Reference PhysicalModelGenerator physicalModelGenerator) {
        super(ComponentNames.RUNTIME_URI,
              generatorRegistry,
              wireResolver,
              normalizer,
              allocator,
              routingService,
              store,
              metaDataStore,
              physicalModelGenerator);
    }

    public void instantiateHostComponentDefinition(ComponentDefinition<?> definition) throws InstantiationException {
        LogicalComponent<?> component = instantiate(domain, definition);
        domain.addComponent(component);
    }
}
