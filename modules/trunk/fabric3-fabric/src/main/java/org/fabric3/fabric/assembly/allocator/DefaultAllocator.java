/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.assembly.allocator;

import java.net.URI;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.topology.RuntimeInfo;
import org.fabric3.spi.model.type.CompositeImplementation;

/**
 * Default Allocator implementation.
 *
 * @version $Rev$ $Date$
 */
public class DefaultAllocator implements Allocator {
    private HostInfo hostInfo;

    public DefaultAllocator(@Reference HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    public void allocate(Map<String, RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {
        if (CompositeImplementation.class.isInstance(component.getDefinition().getImplementation())) {
            for (LogicalComponent<?> child : component.getComponents()) {
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
    private void assign(Map<String, RuntimeInfo> runtimes, LogicalComponent<?> component) throws AllocationException {
        RuntimeInfo info = null;
        URI id = component.getRuntimeId();
        if (id != null) {
            info = runtimes.get(id.toString());
        }
        if (info == null) {
            // Assign runtime using a simple algorithm: if two or more exist, pick one other than the controller,
            // otherwise deploy locally
            if (runtimes.size() < 2) {
                // single node setup, allocate locally
                component.setRuntimeId(null);
                return;
            }
            for (RuntimeInfo runtime : runtimes.values()) {
                if (!hostInfo.getRuntimeId().equals(runtime.getId())) {
                    info = runtime;
                    break;
                }
            }
            if (info != null) {
                component.setRuntimeId(URI.create(info.getId()));
            } else {
                component.setRuntimeId(null);
            }
        }
    }

}
