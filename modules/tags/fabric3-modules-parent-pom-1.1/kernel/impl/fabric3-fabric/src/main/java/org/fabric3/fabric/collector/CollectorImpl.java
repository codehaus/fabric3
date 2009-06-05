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
package org.fabric3.fabric.collector;

import java.util.Iterator;
import java.util.Set;
import javax.xml.namespace.QName;

import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Default Collector implementation.
 *
 * @version $Revision$ $Date$
 */
public class CollectorImpl implements Collector {

    public void markAsProvisioned(LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            // note: all components must be traversed as new wires could be deployed to an existing provisioned component
            if (component instanceof LogicalCompositeComponent) {
                markAsProvisioned((LogicalCompositeComponent) component);
            }
            if (LogicalState.NEW == component.getState()) {
                component.setState(LogicalState.PROVISIONED);
            }
            for (LogicalService service : component.getServices()) {
                for (LogicalBinding<?> binding : service.getBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
                for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
            }
            for (LogicalReference reference : component.getReferences()) {
                for (LogicalBinding<?> binding : reference.getBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
                for (LogicalBinding<?> binding : reference.getCallbackBindings()) {
                    if (LogicalState.NEW == binding.getState()) {
                        binding.setState(LogicalState.PROVISIONED);
                    }
                }
            }
        }
        for (Set<LogicalWire> set : composite.getWires().values()) {
            for (LogicalWire wire : set) {
                if (LogicalState.NEW == wire.getState()) {
                    wire.setState(LogicalState.PROVISIONED);
                }
            }
        }
    }


    public void markForCollection(QName deployable, LogicalCompositeComponent composite) {
        for (LogicalComponent<?> component : composite.getComponents()) {
            if (deployable.equals(component.getDeployable())) {
                if (component instanceof LogicalCompositeComponent) {
                    markForCollection(deployable, (LogicalCompositeComponent) component);
                }
                component.setState(LogicalState.MARKED);
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        binding.setState(LogicalState.MARKED);
                    }
                }
                for (LogicalReference reference : component.getReferences()) {
                    for (LogicalBinding<?> binding : reference.getBindings()) {
                        binding.setState(LogicalState.MARKED);
                    }
                    for (LogicalWire wire : composite.getWires(reference)) {
                        wire.setState(LogicalState.MARKED);
                    }
                }
            } else {
                // mark service and callback bindings that were dynamically added to satisfy a wire when the deployable was provisioned
                for (LogicalService service : component.getServices()) {
                    for (LogicalBinding<?> binding : service.getBindings()) {
                        if (deployable.equals(binding.getDeployable())) {
                            binding.setState(LogicalState.MARKED);
                        }
                    }
                    for (LogicalBinding<?> binding : service.getCallbackBindings()) {
                        if (deployable.equals(binding.getDeployable())) {
                            binding.setState(LogicalState.MARKED);
                        }
                    }
                }
                // recurse through wires and mark any that were part of the deployable being undeployed
                // this can occur when a wire is configured in a deployable other than its source component
                for (Set<LogicalWire> set : composite.getWires().values()) {
                    for (LogicalWire wire : set) {
                        if (LogicalState.MARKED == wire.getState()) {
                            continue;
                        }
                        if (deployable.equals(wire.getTargetDeployable())) {
                            wire.setState(LogicalState.MARKED);
                        }
                    }
                }
            }
        }
    }

    public void collect(LogicalCompositeComponent composite) {
        Iterator<LogicalComponent<?>> iter = composite.getComponents().iterator();
        while (iter.hasNext()) {
            LogicalComponent<?> component = iter.next();
            if (LogicalState.MARKED == component.getState()) {
                iter.remove();
            } else {
                for (LogicalService service : component.getServices()) {
                    removeMarkedBindings(service.getBindings().iterator());
                    removeMarkedBindings(service.getCallbackBindings().iterator());
                }
                for (LogicalReference reference : component.getReferences()) {
                    removeMarkedBindings(reference.getBindings().iterator());
                    removeMarkedBindings(reference.getCallbackBindings().iterator());
                }
                if (component instanceof LogicalCompositeComponent) {
                    collect((LogicalCompositeComponent) component);
                }
            }
        }
        for (Set<LogicalWire> set : composite.getWires().values()) {
            for (Iterator<LogicalWire> it = set.iterator(); it.hasNext();) {
                LogicalWire wire = it.next();
                if (LogicalState.MARKED == wire.getState()) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Removes marked bindings
     *
     * @param iter the collection of bindings to iterate
     */
    private void removeMarkedBindings(Iterator<LogicalBinding<?>> iter) {
        while (iter.hasNext()) {
            LogicalBinding<?> binding = iter.next();
            if (LogicalState.MARKED == binding.getState()) {
                iter.remove();
            }
        }
    }


}
