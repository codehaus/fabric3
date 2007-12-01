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
import org.fabric3.fabric.domain.DomainService;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.osoa.sca.annotations.Reference;

/**
 * The default RuntimeAssembly implementation
 *
 * @version $Rev$ $Date$
 */
public class RuntimeAssemblyImpl extends AbstractAssembly implements RuntimeAssembly {
    
    private final DomainService domainService;
    
    public RuntimeAssemblyImpl(@Reference Allocator allocator,
                               @Reference RoutingService routingService,
                               @Reference MetaDataStore metadataStore,
                               @Reference PhysicalModelGenerator physicalModelGenerator,
                               @Reference LogicalModelGenerator logicalModelGenerator,
                               @Reference DomainService domainService,
                               @Reference PhysicalWireGenerator wireGenerator) {
        super(allocator, routingService, metadataStore, physicalModelGenerator, logicalModelGenerator, domainService, wireGenerator);
        this.domainService = domainService;
    }

    public void instantiateHostComponentDefinition(ComponentDefinition<?> definition) throws ActivateException {
        
        LogicalComponent<CompositeImplementation> domain = domainService.getDomain();
        LogicalComponent<?> component = instantiate(domain, definition);
        domain.addComponent(component);
    }
    
}
