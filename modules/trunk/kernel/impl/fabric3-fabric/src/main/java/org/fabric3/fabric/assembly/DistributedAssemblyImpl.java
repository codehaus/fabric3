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

import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.services.contribution.MetaDataStore;

/**
 * Default implementation of a DistributedAssembly
 *
 * @version $Rev$ $Date$
 */
public class DistributedAssemblyImpl extends AbstractAssembly implements Assembly {
    private LogicalComponentManager logicalComponentManager;

    public DistributedAssemblyImpl(@Reference Allocator allocator,
                                   @Reference(name = "store")MetaDataStore metaDataStore,
                                   @Reference PhysicalModelGenerator physicalModelGenerator,
                                   @Reference LogicalModelInstantiator logicalModelInstantiator,
                                   @Reference(name = "logicalComponentManager")LogicalComponentManager logicalComponentManager,
                                   @Reference RoutingService routingService) {
        super(allocator, metaDataStore, physicalModelGenerator, logicalModelInstantiator, logicalComponentManager, routingService);
        this.logicalComponentManager = logicalComponentManager;
    }

    public void initialize() throws AssemblyException {
        logicalComponentManager.initialize();
    }
}
