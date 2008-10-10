/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.allocator;

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.services.discovery.DiscoveryService;
import org.fabric3.spi.services.runtime.RuntimeInfoService;

/**
 * Default Allocator implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultAllocator implements Allocator {
    private URI runtimeId;
    private DiscoveryService discoveryService;
    private long syncPause = 1000;
    private int syncTimes = 1000;


    public DefaultAllocator(@Reference RuntimeInfoService runtimeInfoService,
                            @Reference DiscoveryService discoveryService) {
        this.runtimeId = runtimeInfoService.getCurrentRuntimeId();
        this.discoveryService = discoveryService;
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

    /**
     * Sets the pause time in milliseconds for service node polling during a topology sync operation
     *
     * @param syncPause the pause time in milliseconds
     */
    @Property(required = false)
    public void setSyncPause(long syncPause) {
        this.syncPause = syncPause;
    }

    public void allocate(LogicalComponent<?> component, boolean synchronizeTopology) throws AllocationException {
        if (synchronizeTopology) {
            synchronizeTopology(component);
        }
        Set<RuntimeInfo> runtimes = discoveryService.getParticipatingRuntimes();
        allocate(runtimes, component);
    }

    private void allocate(Set<RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {

        if (CompositeImplementation.class.isInstance(component.getDefinition().getImplementation())) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                if (CompositeImplementation.class.isInstance(child.getDefinition().getImplementation())) {
                    // the component is a composite, recurse and asign its children
                    allocate(runtimes, child);
                } else {
                    assign(runtimes, child);
                }
            }
        } else {
            assign(runtimes, component);
        }

    }


    /**
     * Assigns a component to a runtime
     *
     * @param runtimes  the list of available runtimes
     * @param component the component to assign
     * @throws AllocationException if an error occurs assigning the component
     */
    private void assign(Set<RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {
        RuntimeInfo info = null;
        if (!runtimes.contains(info)) {
            // Assign runtime using a simple algorithm: if two or more exist, pick one other than the controller,
            // otherwise deploy locally
            if (runtimes.size() < 1) {
                // single node setup, allocate locally
                component.setZone(null);
                return;
            }

            for (RuntimeInfo runtime : runtimes) {
                if (!runtimeId.equals(runtime.getId())) {
                    info = runtime;
                    break;
                }
            }
            if (info != null) {
                component.setZone(info.getId());
            } else {
                component.setZone(null);
            }
        }
    }


    /**
     * Attempts to synchrnize the domain topological view with runtime nodes the component or its children have been pre-allocated to. The list of
     * runtimes are periodically queried a set number of times. It is assumed the list of runtimes will be asynchronously updated as new nodes are
     * discovered. If a pre-allocated runtime is not found for a component, the latter is marked for re-allocation.
     *
     * @param component to synchronize the domain topology with
     */
    private void synchronizeTopology(LogicalComponent<?> component) {
        // calculate the set of runtimes the component or its children (if it is a composite) have been pre-allocated to
        Set<URI> preAllocated = calculatePreallocatedRuntimes(component);
        // synchronize the set of runtimes with the domain topology, gathering the non-responding runtimes
        Set<URI> nonRespondingRuntimes = new HashSet<URI>();
        for (URI runtime : preAllocated) {
            int i = 0;
            while (!getRuntimes().containsKey(runtime) && i < syncTimes) {
                try {
                    Thread.sleep(syncPause);
                    ++i;
                } catch (InterruptedException e) {
                    throw new AssertionError();
                }
            }
            if (!getRuntimes().containsKey(runtime)) {
                nonRespondingRuntimes.add(runtime);
            }
        }
        // mark components pre-allocated to a non-responding runtime as needing to be re-allocated
        markForReallocation(component, nonRespondingRuntimes);
    }

    private Map<URI, RuntimeInfo> getRuntimes() {
        Map<URI, RuntimeInfo> runtimes = new HashMap<URI, RuntimeInfo>();
        Set<RuntimeInfo> infos = discoveryService.getParticipatingRuntimes();
        for (RuntimeInfo info : infos) {
            runtimes.put(info.getId(), info);
        }
        return runtimes;
    }

    /**
     * Returns the set of pre-allocated runtimes for a component and its children.
     *
     * @param component the component being allocated
     * @return the set of pre-allocated runtimes for a component and its children
     */
    private HashSet<URI> calculatePreallocatedRuntimes(LogicalComponent<?> component) {
        HashSet<URI> runtimes = new HashSet<URI>();
        calculatePreallocatedRuntimes(component, runtimes);
        return runtimes;
    }

    @SuppressWarnings({"unchecked"})
    private void calculatePreallocatedRuntimes(LogicalComponent<?> component, Set<URI> runtimes) {

        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                calculatePreallocatedRuntimes(child, runtimes);
            }
        } else {
            URI uri = component.getZone();
            if (uri != null) {
                if (!runtimes.contains(uri)) {
                    runtimes.add(uri);
                }
            }
        }
    }

    /**
     * Marks a component or its children for re-allocation if its pre-allocated runtime is in the set of non-responding runtimes.
     *
     * @param component             the component to evaluate
     * @param nonRespondingRuntimes the list of non-responding runtimes
     */
    private void markForReallocation(LogicalComponent<?> component, Set<URI> nonRespondingRuntimes) {
        if (!CompositeImplementation.class.isInstance(component.getDefinition().getImplementation())) {
            URI id = component.getZone();
            if (id != null && nonRespondingRuntimes.contains(id)) {
                component.setZone(null);
                component.setActive(false);
            } else if (id != null) {
                // check to see if the component is already running on the service node, and if so record that it is running
                RuntimeInfo info = getRuntimes().get(id);
                assert info != null;
                if (info.getComponents().contains(component.getUri())) {
                    component.setActive(true);
                } else {
                    component.setActive(false);
                }

            }
        }


        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                markForReallocation(child, nonRespondingRuntimes);
            }
        }

    }


}
