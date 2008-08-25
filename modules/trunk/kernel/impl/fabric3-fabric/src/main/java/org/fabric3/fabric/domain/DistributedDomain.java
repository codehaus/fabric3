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
import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.host.domain.DomainException;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.lcm.RecoveryException;

/**
 * Implements a distributed domain containing user-defined services.
 *
 * @version $Rev$ $Date$
 */
public class DistributedDomain extends AbstractDomain implements Domain {
    private LogicalComponentManager logicalComponentManager;

    public DistributedDomain(@Reference(name = "store")MetaDataStore metaDataStore,
                             @Reference(name = "logicalComponentManager")LogicalComponentManager logicalComponentManager,
                             @Reference Allocator allocator,
                             @Reference PhysicalModelGenerator physicalModelGenerator,
                             @Reference LogicalModelInstantiator logicalModelInstantiator,
                             @Reference BindingSelector bindingSelector,
                             @Reference RoutingService routingService) {
        super(metaDataStore, logicalComponentManager, allocator, physicalModelGenerator, logicalModelInstantiator, bindingSelector, routingService);
        this.logicalComponentManager = logicalComponentManager;
    }

    /**
     * Used to reinject the Allocator. This allows an alternative allocation mechanism to be used by adding an optional extension to the runtime.
     *
     * @param allocator the allocator to override the default one
     */
    @Reference
    public void setAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

    public void initialize() throws DomainException {
        try {
            logicalComponentManager.initialize();
        } catch (RecoveryException e) {
            throw new DomainException(e);
        }
    }
}
