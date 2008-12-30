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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.fabric.instantiator.TargetResolutionService;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Resolves an explicit target uri specified on a reference to a service and creates a corresponding wire.
 *
 * @version $Revsion$ $Date$
 */
public class ExplicitTargetResolutionService implements TargetResolutionService {
    private ServiceContractResolver contractResolver;

    public ExplicitTargetResolutionService(@Reference ServiceContractResolver contractResolver) {
        this.contractResolver = contractResolver;
    }

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent component, LogicalChange change) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            // the reference is not configured on the component definition in the composite
            return;
        }

        List<URI> requestedTargets = componentReference.getTargets();
        if (requestedTargets.isEmpty()) {
            // no target urls are specified
            return;
        }

        URI parentUri = component.getUri();
        URI componentUri = logicalReference.getParent().getUri();

        // resolve the target URIs to services
        List<URI> targets = new ArrayList<URI>();
        for (URI requestedTarget : requestedTargets) {
            URI resolved = parentUri.resolve(componentUri).resolve(requestedTarget);
            URI targetURI = resolveByUri(logicalReference, resolved, component, change);
            if (targetURI == null) {
                return;
            }
            targets.add(targetURI);

        }
        // create the logical wires
        // xcv potentially remove if LogicalWires added to LogicalReference
        LogicalComponent parent = logicalReference.getParent();
        LogicalCompositeComponent grandParent = (LogicalCompositeComponent) parent.getParent();
        Set<LogicalWire> wires = new LinkedHashSet<LogicalWire>();
        if (null != grandParent) {
            for (URI targetUri : targets) {
                LogicalWire wire = new LogicalWire(grandParent, logicalReference, targetUri);
                change.addWire(wire);
                wires.add(wire);
            }
            grandParent.overrideWires(logicalReference, wires);
        } else {
            for (URI targetUri : targets) {
                LogicalWire wire = new LogicalWire(parent, logicalReference, targetUri);
                change.addWire(wire);
                wires.add(wire);
            }
            ((LogicalCompositeComponent) parent).overrideWires(logicalReference, wires);
        }
        // end remove
        logicalReference.setResolved(true);
    }

    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite, LogicalChange change) {

        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);
        if (targetComponent == null) {
            URI referenceUri = reference.getUri();
            URI componentUri = reference.getParent().getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            TargetComponentNotFound error = new TargetComponentNotFound(referenceUri, targetComponentUri, componentUri, contributionUri);
            change.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        LogicalService targetService = null;
        if (serviceName != null) {
            targetService = targetComponent.getService(serviceName);
            if (targetService == null) {
                URI name = UriHelper.getDefragmentedName(targetUri);
                URI uri = reference.getUri();
                String msg = "The service " + serviceName + " targeted from the reference " + uri + " is not found on component " + name;
                URI componentUri = reference.getParent().getUri();
                ServiceNotFound error = new ServiceNotFound(msg, uri, componentUri, targetComponentUri);
                change.addError(error);
                return null;
            }
        } else {
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (targetService != null) {
                    String msg = "More than one service available on component: " + targetUri
                            + ". Reference must explicitly specify a target service: " + reference.getUri();
                    LogicalComponent<?> parent = reference.getParent();
                    URI componentUri = parent.getUri();
                    URI contributionUri = parent.getDefinition().getContributionUri();
                    AmbiguousService error = new AmbiguousService(msg, componentUri, contributionUri);
                    change.addError(error);
                    return null;
                }
                targetService = service;
            }
            if (targetService == null) {
                String msg = "The reference " + reference.getUri() + " is wired to component " + targetUri + " but the component has no services";
                LogicalComponent<?> parent = reference.getParent();
                URI componentUri = parent.getUri();
                URI contributionUri = parent.getDefinition().getContributionUri();
                NoServiceOnComponent error = new NoServiceOnComponent(msg, componentUri, contributionUri);
                change.addError(error);
                return null;
            }
        }
        validateContracts(reference, targetService, change);
        return targetService.getUri();
    }

    /**
     * Validates the reference and service contracts match.
     *
     * @param reference the reference
     * @param service   the service
     * @param change    the logical change
     */
    private void validateContracts(LogicalReference reference, LogicalService service, LogicalChange change) {
        ServiceContract<?> referenceContract = contractResolver.determineContract(reference);
        ServiceContract<?> serviceContract = contractResolver.determineContract(service);
        if (!referenceContract.isAssignableFrom(serviceContract)) {
            URI uri = reference.getParent().getUri();
            URI referenceUri = reference.getUri();
            URI serviceUri = service.getUri();
            URI contributionUri = reference.getParent().getDefinition().getContributionUri();
            IncompatibleContracts error = new IncompatibleContracts(referenceUri, serviceUri, uri, contributionUri);
            change.addError(error);
        }
    }

}
