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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.assembly.allocator.AllocationException;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.assembly.store.AssemblyStore;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.Referenceable;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of a DistributedAssembly
 *
 * @version $Rev$ $Date$
 */
@Service(DistributedAssembly.class)
@EagerInit
public class DistributedAssemblyImpl extends AbstractAssembly implements DistributedAssembly {
    private Allocator allocator;
    private DiscoveryService discoveryService;
    private long syncPause = 1000;
    private int syncTimes = 10;

    public DistributedAssemblyImpl(@Reference GeneratorRegistry generatorRegistry,
                                   @Reference WireResolver wireResolver,
                                   @Reference PromotionNormalizer normalizer,
                                   @Reference Allocator allocator,
                                   @Reference DiscoveryService discoveryService,
                                   @Reference RoutingService routingService,
                                   @Reference AssemblyStore store,
                                   @Reference MetaDataStore metaDataStore,
                                   @Reference HostInfo hostInfo) {
        super(hostInfo.getDomain(),
              generatorRegistry,
              wireResolver,
              normalizer,
              routingService,
              store,
              metaDataStore);
        this.allocator = allocator;
        this.discoveryService = discoveryService;
    }

    /**
     * Sets the pause time in milliseconds for service node polling during a topology sync operation
     *
     * @param syncPause the pause time in milliseconds
     */
    @Property(required = false)
    public void setSyncPause(long syncPause) {
        this.syncPause = syncPause;
    }

    /**
     * Sets the number of times a service node should be polled during a topology sync operation.
     *
     * @param syncTimes the number of times a service node should be polled during a topology sync operation
     */
    @Property(required = false)
    public void setSyncTimes(int syncTimes) {
        this.syncTimes = syncTimes;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void allocate(LogicalComponent<?> component, boolean synchronizeTopology) throws AllocationException {
        if (synchronizeTopology) {
            synchronizeTopology(component);
            // TODO determine the list of components to recover
        }
        allocator.allocate(discoveryService.getParticipatingRuntimes(), component);
    }

    @Override
    protected Referenceable resolveTarget(URI uri, LogicalComponent<CompositeImplementation> component)
            throws ResolutionException {
        // TODO only resolves one level deep
        URI defragmentedUri = UriHelper.getDefragmentedName(uri);
        Referenceable target = component.getComponent(defragmentedUri);
        if (target != null) {
            return target;
        }
        target = component.getReference(uri.getFragment());
        if (target != null) {
            return target;
        }
        throw new TargetNotFoundException("Target not found", uri.toString());
    }


    /**
     * Attempts to synchrnize the domain topological view with runtime nodes the component or its children have been
     * pre-allocated to. The list of runtimes are periodically queried a set number of times. It is assumed the list of
     * runtimes will be asynchronously updates as new nodes are discovered. If a pre-allocated runtime is not found for
     * a component, the latter is marked for re-allocation.
     *
     * @param component to synchronize the domain topology with
     */
    private void synchronizeTopology(LogicalComponent<?> component) {
        // calculate the set of runtimes the component or its children (if it is a composite) have been pre-allocated to
        Set<String> preAllocated = calculatePreallocatedRuntimes(component);
        // synchronize the set of runtimes with the domain topology, gathering the non-responding runtimes
        Set<String> nonRespondingRuntimes = new HashSet<String>();
        Map<String, RuntimeInfo> runtimes = new HashMap<String, RuntimeInfo>();
        Set<RuntimeInfo> infos = discoveryService.getParticipatingRuntimes();
        for (RuntimeInfo info : infos) {
            runtimes.put(info.getId(), info);
        }
        for (String runtime : preAllocated) {
            int i = 0;
            while (!runtimes.containsKey(runtime) && i < syncTimes) {
                try {
                    Thread.sleep(syncPause);
                    ++i;
                } catch (InterruptedException e) {
                    throw new AssertionError();
                }
            }
            if (!runtimes.containsKey(runtime)) {
                nonRespondingRuntimes.add(runtime);
            }
        }
        // mark components pre-allocated to a non-responding runtime as needing to be re-allocated
        markForReallocation(component, nonRespondingRuntimes);
    }

    /**
     * Returns the set of pre-allocated runtimes for a component and its children.
     *
     * @param component the component being allocated
     * @return the set of pre-allocated runtimes for a component and its children
     */
    private HashSet<String> calculatePreallocatedRuntimes(LogicalComponent<?> component) {
        HashSet<String> runtimes = new HashSet<String>();
        calculatePreallocatedRuntimes(component, runtimes);
        return runtimes;
    }

    @SuppressWarnings({"unchecked"})
    private void calculatePreallocatedRuntimes(LogicalComponent<?> component, Set<String> runtimes) {
        for (LogicalComponent<?> child : component.getComponents()) {
            if (CompositeImplementation.class.isInstance(child.getDefinition().getImplementation())) {
                calculatePreallocatedRuntimes(child, runtimes);
            } else {
                URI uri = child.getRuntimeId();
                if (uri != null) {
                    String runtime = uri.toString();
                    if (!runtimes.contains(runtime)) {
                        runtimes.add(runtime);
                    }
                }
            }
        }
    }

    /**
     * Marks a component or its children for re-allocation if its pre-allocated runtime is in the set of non-responding
     * runtimes.
     *
     * @param component             the component to evaluate
     * @param nonRespondingRuntimes the list of non-responding runtimes
     */
    private void markForReallocation(LogicalComponent<?> component, Set<String> nonRespondingRuntimes) {
        URI id = component.getRuntimeId();
        if (id != null && nonRespondingRuntimes.contains(id.toString())) {
            component.setRuntimeId(null);
        }
        for (LogicalComponent<?> child : component.getComponents()) {
            markForReallocation(child, nonRespondingRuntimes);
        }
    }


}
