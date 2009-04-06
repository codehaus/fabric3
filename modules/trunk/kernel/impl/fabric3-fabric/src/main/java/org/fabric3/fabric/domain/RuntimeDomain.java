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

import org.fabric3.fabric.binding.BindingSelector;
import org.fabric3.fabric.collector.Collector;
import org.fabric3.fabric.generator.Generator;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.domain.RoutingService;
import org.fabric3.spi.policy.PolicyRegistry;
import org.fabric3.spi.policy.PolicyResolver;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * Implements a domain for system components in a runtime. Fabric3 runtimes are constituted using SCA components and the runtime domain manages
 * deployment of those system components. When a runtime is booted, the runtime domain is provided with a set of primoridal services for deploying
 * system components. After bootstrap, the runtime domain is reinjected with a new set of fully-configured deployment services.
 *
 * @version $Rev$ $Date$
 */
public class RuntimeDomain extends AbstractDomain {

    public RuntimeDomain(@Reference MetaDataStore metadataStore,
                         @Reference Generator generator,
                         @Reference LogicalModelInstantiator logicalModelInstantiator,
                         @Reference PolicyResolver policyResolver,
                         @Reference LogicalComponentManager logicalComponentManager,
                         @Reference BindingSelector bindingSelector,
                         @Reference RoutingService routingService,
                         @Reference Collector collector,
                         @Reference HostInfo info) {
        super(metadataStore,
              logicalComponentManager,
              generator,
              logicalModelInstantiator,
              policyResolver,
              bindingSelector,
              routingService,
              collector,
              info);
    }

    /**
     * Used for reinjection.
     *
     * @param generator the generator to inject
     */
    @Reference
    public void setGenerator(Generator generator) {
        this.generator = generator;
    }

    /**
     * Used for reinjection.
     *
     * @param routingService the routing service to reinject
     */
    @Reference
    public void setRoutingService(RoutingService routingService) {
        this.routingService = routingService;
    }

    /**
     * Used to inject the PolicyRegistry after bootstrap.
     *
     * @param policyRegistry the registry
     */
    @Reference(required = false)
    public void setPolicyRegistry(PolicyRegistry policyRegistry) {
        this.policyRegistry = policyRegistry;
    }

}
