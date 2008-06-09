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
package org.fabric3.fabric.domain;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.allocator.Allocator;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Implements a domain for system services in a runtime. As Fabric3 runtimes are constituted using SCA, the runtime domain manages system services.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeDomain extends AbstractDomain {

    public RuntimeDomain(@Reference Allocator allocator,
                         @Reference MetaDataStore metadataStore,
                         @Reference PhysicalModelGenerator physicalModelGenerator,
                         @Reference LogicalModelInstantiator logicalModelInstantiator,
                         @Reference LogicalComponentManager logicalComponentManager,
                         @Reference RoutingService routingService) {
        super(allocator, metadataStore, physicalModelGenerator, logicalModelInstantiator, logicalComponentManager, routingService);
    }

}
