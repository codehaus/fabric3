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

import java.util.Collection;
import java.util.Map;
import java.net.URI;

import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.allocator.AllocationException;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.generator.GeneratorContext;

import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Default implementation of a DistributedAssembly
 *
 * @version $Rev$ $Date$
 */
@Service(DistributedAssembly.class)
public class DistributedAssemblyImpl extends AbstractAssembly implements DistributedAssembly {
    
    public DistributedAssemblyImpl(@Reference Allocator allocator,
                                   @Reference RoutingService routingService,
                                   @Reference(name = "store") MetaDataStore metaDataStore,
                                   @Reference PhysicalModelGenerator physicalModelGenerator,
                                   @Reference LogicalModelGenerator logicalModelGenerator,
                                   @Reference(name="logicalComponentManager") LogicalComponentManager logicalComponentManager,
                                   @Reference PhysicalWireGenerator wireGenerator,
                                   @Reference WireResolver wireResolver) {
        super(allocator, routingService, metaDataStore, physicalModelGenerator, logicalModelGenerator,
              logicalComponentManager, wireGenerator, wireResolver);
    }

    public void initialize() throws AssemblyException {
        logicalComponentManager.initialize();
        Collection<LogicalComponent<?>> components = logicalComponentManager.getComponents();

        try {
            for (LogicalComponent<?> component : components) {
                allocator.allocate(component, false);
            }
        } catch (AllocationException e) {
            throw new ActivateException(e);
        }

        // generate and provision components on nodes that have gone down
        Map<URI, GeneratorContext> contexts = physicalModelGenerator.generate(components);
        physicalModelGenerator.provision(contexts);
        // TODO end temporary recovery code

    }
}
