/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.domain;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.allocator.Allocator;
import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.spi.services.routing.RoutingService;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Implements a distributed domain containing user-defined services.
 *
 * @version $Rev$ $Date$
 */
public class DistributedDomain extends AbstractDomain implements Domain {

    public DistributedDomain(@Reference(name = "store") MetaDataStore metaDataStore,
                             @Reference(name = "logicalComponentManager") LogicalComponentManager logicalComponentManager,
                             @Reference PhysicalModelGenerator physicalModelGenerator,
                             @Reference LogicalModelInstantiator logicalModelInstantiator,
                             @Reference BindingSelector bindingSelector,
                             @Reference RoutingService routingService) {
        super(metaDataStore, logicalComponentManager, physicalModelGenerator, logicalModelInstantiator, bindingSelector, routingService);
    }

    /**
     * Used to optionally inject an Allocator.
     *
     * @param allocator the allocator
     */
    @Reference(required = false)
    public void setAllocator(Allocator allocator) {
        this.allocator = allocator;
    }

}
