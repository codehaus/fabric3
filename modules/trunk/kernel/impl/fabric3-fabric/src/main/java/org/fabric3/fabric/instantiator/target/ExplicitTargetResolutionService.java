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
package org.fabric3.fabric.instantiator.target;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fabric3.fabric.instantiator.AmbiguousService;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.NoServiceOnComponent;
import org.fabric3.fabric.instantiator.ServiceNotFound;
import org.fabric3.scdl.ComponentReference;
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

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent component, LogicalChange change) {

        ComponentReference componentReference = logicalReference.getComponentReference();
        if (componentReference == null) {
            return;
        }

        List<URI> requestedTargets = componentReference.getTargets();
        if (requestedTargets.isEmpty()) {
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
    }

    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite, LogicalChange change) {

        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);
        if (targetComponent == null) {
            TargetComponentNotFound error = new TargetComponentNotFound(reference, targetComponentUri);
            change.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                URI name = UriHelper.getDefragmentedName(targetUri);
                URI uri = reference.getUri();
                String msg = "The service " + serviceName + " targeted from the reference " + uri + " is not found on component " + name;
                ServiceNotFound error = new ServiceNotFound(msg, reference, targetComponentUri);
                change.addError(error);
                return null;
            }
            return targetUri;
        } else {
            LogicalService target = null;
            for (LogicalService service : targetComponent.getServices()) {
                if (service.getDefinition().isManagement()) {
                    continue;
                }
                if (target != null) {
                    String msg = "More than one service available on component: " + targetUri
                            + ". Reference must explicitly specify a target service: " + reference.getUri();
                    AmbiguousService error = new AmbiguousService(reference, msg);
                    change.addError(error);
                    return null;
                }
                target = service;
            }
            if (target == null) {
                String msg = "The reference " + reference.getUri() + " is wired to component " + targetUri + " but the component has no services";
                NoServiceOnComponent error = new NoServiceOnComponent(msg, reference);
                change.addError(error);
                return null;
            }
            return target.getUri();
        }

    }

}
