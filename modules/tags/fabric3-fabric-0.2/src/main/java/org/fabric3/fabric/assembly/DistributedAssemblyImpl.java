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

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.assembly.store.AssemblyStore;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of a DistributedAssembly
 *
 * @version $Rev$ $Date$
 */
@Service(DistributedAssembly.class)
public class DistributedAssemblyImpl extends AbstractAssembly implements DistributedAssembly {

    public DistributedAssemblyImpl(@Reference GeneratorRegistry generatorRegistry,
                                   @Reference WireResolver wireResolver,
                                   @Reference PromotionNormalizer normalizer,
                                   @Reference Allocator allocator,
                                   @Reference RoutingService routingService,
                                   @Reference AssemblyStore store,
                                   @Reference(name = "store")MetaDataStore metaDataStore,
                                   @Reference HostInfo hostInfo) {
        super(hostInfo.getDomain(),
              generatorRegistry,
              wireResolver,
              normalizer,
              allocator,
              routingService,
              store,
              metaDataStore);
    }
}
