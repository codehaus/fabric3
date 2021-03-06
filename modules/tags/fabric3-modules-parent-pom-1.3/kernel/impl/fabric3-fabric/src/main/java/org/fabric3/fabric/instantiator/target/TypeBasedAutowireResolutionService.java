/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.ReferenceNotFound;
import org.fabric3.fabric.instantiator.TargetResolutionService;
import org.fabric3.model.type.component.AbstractComponentType;
import org.fabric3.model.type.component.Autowire;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.contract.ServiceContract;
import org.fabric3.spi.contract.ContractMatcher;
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
    private ServiceContractResolver resolver;
    private ContractMatcher matcher;

    public TypeBasedAutowireResolutionService(@Reference ServiceContractResolver resolver, @Reference ContractMatcher matcher) {
        this.resolver = resolver;
        this.matcher = matcher;
    }

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent compositeComponent, InstantiationContext context) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();

        if (componentReference == null) {
            // The reference is not configured on the component definition in the composite. i.e. it is only present in the componentType
            if (logicalReference.isResolved()) {
                return;
            }

            ServiceContract requiredContract = resolver.determineContract(logicalReference);

            Autowire autowire = calculateAutowire(compositeComponent, component);
            if (autowire == Autowire.ON) {
                resolveByType(compositeComponent, logicalReference, requiredContract);
            }

        } else {
            // The reference is explicity configured on the component definition in the composite
            List<URI> uris = componentReference.getTargets();
            if (!uris.isEmpty()) {
                return;
            }

            if (componentReference.isAutowire()) {
                ReferenceDefinition referenceDefinition = logicalReference.getDefinition();
                ServiceContract requiredContract = referenceDefinition.getServiceContract();
                boolean resolved = resolveByType(component.getParent(), logicalReference, requiredContract);
                if (!resolved) {
                    resolveByType(compositeComponent, logicalReference, requiredContract);
                }
            }
        }

        boolean targetted = !logicalReference.getWires().isEmpty();
        if (!targetted && logicalReference.getDefinition().isRequired() && logicalReference.getBindings().isEmpty()) {
            String referenceUri = logicalReference.getUri().toString();
            URI componentUri = component.getUri();
            URI contributionUri = component.getDefinition().getContributionUri();
            ReferenceNotFound error =
                    new ReferenceNotFound("Unable to resolve reference " + referenceUri, referenceUri, componentUri, contributionUri);
            context.addError(error);
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
     * @param logicalReference the logical reference
     * @param contract         the contract to match against
     * @return true if the reference has been resolved.
     */
    private boolean resolveByType(LogicalCompositeComponent composite, LogicalReference logicalReference, ServiceContract contract) {

        List<LogicalService> candidates = new ArrayList<LogicalService>();
        Multiplicity refMultiplicity = logicalReference.getDefinition().getMultiplicity();
        boolean multiplicity = Multiplicity.ZERO_N.equals(refMultiplicity) || Multiplicity.ONE_N.equals(refMultiplicity);
        for (LogicalComponent<?> child : composite.getComponents()) {
            if (validKey(logicalReference, child)) {  // if the reference is keyed and the target does not have a key, skip
                for (LogicalService service : child.getServices()) {
                    ServiceContract targetContract = resolver.determineContract(service);
                    if (targetContract == null) {
                        // This is a programming error since a non-composite service must have a service contract
                        throw new AssertionError("No service contract specified on service: " + service.getUri());
                    }
                    if (matcher.isAssignableFrom(contract, targetContract)) {
                        candidates.add(service);
                        break;
                    }
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
        for (LogicalService target : candidates) {
            URI uri = target.getUri();
            // for autowires, the deployable of the wire is the target since the wire must be removed when the target is undeployed
            QName deployable = target.getParent().getDeployable();
            LogicalWire wire = new LogicalWire(composite, logicalReference, uri, deployable);

            LogicalComponent parent = logicalReference.getParent();
            LogicalCompositeComponent grandParent = (LogicalCompositeComponent) parent.getParent();
            if (grandParent != null) {
                grandParent.addWire(logicalReference, wire);
            } else {
                ((LogicalCompositeComponent) parent).addWire(logicalReference, wire);
            }

        }

        return true;

    }

    /**
     * Returns true if the reference is not keyed, true if the reference is keyed and the target specifies a key, false if the reference is keyed and
     * the target does not specify a key.
     *
     * @param logicalReference the logical reference
     * @param target           the target
     * @return true if the reference is not keyed, true if the reference is keyed and the target specifies a key, false if the reference is keyed and
     *         the target does not specify a key
     */
    private boolean validKey(LogicalReference logicalReference, LogicalComponent<?> target) {
        return !logicalReference.getDefinition().isKeyed() || target.getDefinition().getKey() != null;
    }
}
