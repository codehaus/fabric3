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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Resolves an uspecified reference target using the SCA autowire algorithm. If a target is found, a corresponding LogicalWire will be created.
 *
 * @version $Revsion$ $Date$
 */
public class TypeBasedAutowireResolutionService implements TargetResolutionService {
    private ServiceContractResolver contractResolver;

    public TypeBasedAutowireResolutionService(@Reference ServiceContractResolver contractResolver) {
        this.contractResolver = contractResolver;
    }

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent compositeComponent, LogicalChange change) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();

        if (componentReference == null) {
            // The reference is not configured on the component definition in the composite. i.e. it is only present in the componentType
            if (!logicalReference.getBindings().isEmpty() || isPromoted(compositeComponent, logicalReference)) {
                return;
            }

            ServiceContract<?> requiredContract = contractResolver.determineContract(logicalReference);

            Autowire autowire = calculateAutowire(compositeComponent, component);
            if (autowire == Autowire.ON) {
                resolveByType(compositeComponent, component, logicalReference, requiredContract, change);
            }

        } else {
            // The reference is explicity configured on the component definition in the composite
            List<URI> uris = componentReference.getTargets();
            if (!uris.isEmpty() || isPromoted(compositeComponent, logicalReference)) {
                return;
            }

            if (componentReference.isAutowire()) {
                ReferenceDefinition referenceDefinition = logicalReference.getDefinition();
                ServiceContract<?> requiredContract = referenceDefinition.getServiceContract();
                boolean resolved = resolveByType(component.getParent(), component, logicalReference, requiredContract, change);
                if (!resolved) {
                    resolveByType(compositeComponent, component, logicalReference, requiredContract, change);
                }
            }
        }

        boolean targetted = !logicalReference.getWires().isEmpty();
        if (!targetted && logicalReference.getDefinition().isRequired() && logicalReference.getBindings().isEmpty()) {
            String uri = logicalReference.getUri().toString();
            change.addError(new ReferenceNotFound("Unable to resolve reference " + uri, component, uri));
        } else if (targetted) {
            logicalReference.setResolved(true);
        }
    }

    /**
     * Determines the autowire setting for a component
     *
     * @param composite the parent the component inherits its default autowire setting from
     * @param component the component
     * @return the autowire setting
     */
    private Autowire calculateAutowire(LogicalComponent<?> composite, LogicalComponent<?> component) {

        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();

        // check for an overridden value
        Autowire overrideAutowire = component.getAutowireOverride();
        if (overrideAutowire == Autowire.OFF || overrideAutowire == Autowire.ON) {
            return overrideAutowire;
        }

        Autowire autowire = definition.getAutowire();
        if (autowire == Autowire.INHERITED) {
            // check in the parent composite definition
            if (component.getParent() != null) {
                ComponentDefinition<? extends Implementation<?>> def = component.getParent().getDefinition();
                AbstractComponentType<?, ?, ?, ?> type = def.getImplementation().getComponentType();
                autowire = (Composite.class.cast(type)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    return autowire;
                }
            }
            // undefined in the original parent or the component is top-level,
            // check in the target
            ComponentDefinition<? extends Implementation<?>> parentDefinition = composite.getDefinition();
            AbstractComponentType<?, ?, ?, ?> parentType = parentDefinition.getImplementation().getComponentType();
            while (Composite.class.isInstance(parentType)) {
                autowire = (Composite.class.cast(parentType)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    break;
                }
                composite = composite.getParent();
                if (composite == null) {
                    break;
                }
                parentDefinition = composite.getDefinition();
                parentType = parentDefinition.getImplementation().getComponentType();
            }
        }

        return autowire;

    }

    /**
     * Attempts to resolve a reference against a composite using the autowire matching algorithm. If the reference is resolved, a LogicalWire or set
     * of LogicalWires is created.
     *
     * @param composite        the composite to resolve against
     * @param component        the component containing the reference
     * @param logicalReference the logical reference
     * @param contract         the contract to match against
     * @param change           the chnage set
     * @return true if the reference has been resolved.
     */
    private boolean resolveByType(LogicalCompositeComponent composite,
                                  LogicalComponent<?> component,
                                  LogicalReference logicalReference,
                                  ServiceContract<?> contract,
                                  LogicalChange change) {

        List<URI> candidates = new ArrayList<URI>();
        Multiplicity refMultiplicity = logicalReference.getDefinition().getMultiplicity();
        boolean multiplicity = Multiplicity.ZERO_N.equals(refMultiplicity) || Multiplicity.ONE_N.equals(refMultiplicity);
        for (LogicalComponent<?> child : composite.getComponents()) {
            for (LogicalService service : child.getServices()) {
                ServiceContract<?> targetContract = contractResolver.determineContract(service);
                if (targetContract == null) {
                    // This is a programming error since a non-composite service must have a service contract
                    throw new AssertionError("No service contract specified on service: " + service.getUri());
                }
                if (contract.isAssignableFrom(targetContract)) {
                    candidates.add(service.getUri());
                    break;
                }
            }
            if (!candidates.isEmpty() && !multiplicity) {
                // since the reference is to a single target and a candidate has been found, avoid iterating the remaining components
                break;
            }
        }
        if (candidates.isEmpty()) {
            return false;
        }
        // create the wires
        for (URI target : candidates) {
            URI uri = component.getUri().resolve(target);
            LogicalWire wire = new LogicalWire(composite, logicalReference, uri);

            // xcv potentially remove if LogicalWires added to LogicalReference
            LogicalComponent parent = logicalReference.getParent();
            LogicalCompositeComponent grandParent = (LogicalCompositeComponent) parent.getParent();
            if (grandParent != null) {
                grandParent.addWire(logicalReference, wire);
            } else {
                ((LogicalCompositeComponent) parent).addWire(logicalReference, wire);
            }
            // end remove
            change.addWire(wire);

        }

        return true;

    }

    private boolean isPromoted(LogicalComponent<?> composite, LogicalReference logicalReference) {
        LogicalComponent<?> component = logicalReference.getParent();
        for (LogicalReference compositeReference : composite.getReferences()) {
            List<URI> uris = compositeReference.getPromotedUris();
            if (component.getReferences().size() == 1) {
                LogicalReference componentRef = component.getReferences().iterator().next();
                for (URI uri : uris) {
                    if (uri.getFragment() == null && component.getUri().equals(uri)) {
                        return true;
                    } else {
                        if (componentRef.getUri().equals(uri)) {
                            return true;
                        }
                    }
                }
            } else {
                for (URI uri : uris) {
                    if (logicalReference.getUri().equals(uri)) {
                        return true;
                    }
                }

            }
        }
        return false;

    }

}
