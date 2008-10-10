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
package org.fabric3.fabric.allocator;

import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * Default Allocator implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultAllocator implements Allocator {


    public DefaultAllocator() {
    }

    public void allocate(LogicalComponent<?> component, boolean synchronizeTopology) throws AllocationException {
        // Comment out for now until zones are integrated

//        if (synchronizeTopology) {
//            // TODO This should include logic to recover, i.e. when a controller comes up if it needs to synchronize its view with any zones
//        }
//        Set<RuntimeInfo> runtimes = discoveryService.getParticipatingRuntimes();
//        allocate(runtimes, component);
    }

//    private void allocate(Set<RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {
//
//        if (CompositeImplementation.class.isInstance(component.getDefinition().getImplementation())) {
//            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
//            for (LogicalComponent<?> child : composite.getComponents()) {
//                if (CompositeImplementation.class.isInstance(child.getDefinition().getImplementation())) {
//                    // the component is a composite, recurse and asign its children
//                    allocate(runtimes, child);
//                } else {
//                    assign(runtimes, child);
//                }
//            }
//        } else {
//            assign(runtimes, component);
//        }
//
//    }
//
//
//    /**
//     * Assigns a component to a runtime
//     *
//     * @param runtimes  the list of available runtimes
//     * @param component the component to assign
//     * @throws AllocationException if an error occurs assigning the component
//     */
//    private void assign(Set<RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {
//        RuntimeInfo info = null;
//        if (!runtimes.contains(info)) {
//            // Assign runtime using a simple algorithm: if two or more exist, pick one other than the controller,
//            // otherwise deploy locally
//            if (runtimes.size() < 1) {
//                // single node setup, allocate locally
//                component.setZone(null);
//                return;
//            }
//
//            for (RuntimeInfo runtime : runtimes) {
//                if (!runtimeId.equals(runtime.getId())) {
//                    info = runtime;
//                    break;
//                }
//            }
//            if (info != null) {
//                component.setZone(info.getId().toString());
//            } else {
//                component.setZone(null);
//            }
//        }
//    }

}
