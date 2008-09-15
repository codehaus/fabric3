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
package org.fabric3.fabric.instantiator.component;

import java.net.URI;

import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.WireDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.util.UriHelper;

/**
 * Default implementation of the WireInstantiator.
 *
 * @version $Revision$ $Date$
 */
public class WireInstantiatorImpl implements WireInstantiator {

    public void instantiateWires(Composite composite, LogicalCompositeComponent parent, LogicalChange change) {
        String baseUri = parent.getUri().toString();
        // instantiate wires held directly in the composite and in included composites
        for (WireDefinition definition : composite.getWires()) {
            // resolve the source reference
            // source URI is relative to the parent composite the include is targeted to
            URI sourceUri = URI.create(baseUri + "/" + UriHelper.getDefragmentedName(definition.getSource()));
            String referenceName = definition.getSource().getFragment();
            LogicalReference logicalReference = resolveLogicalReference(referenceName, sourceUri, parent, change);
            if (logicalReference == null) {
                // error resolving, continue
                continue;
            }

            // resolve the target service
            URI targetUri = URI.create(baseUri + "/" + definition.getTarget());
            targetUri = resolveTargetUri(targetUri, parent, change);
            if (targetUri == null) {
                // error resolving
                continue;
            }

            // create the wire
            LogicalWire wire = new LogicalWire(parent, logicalReference, targetUri);
            parent.addWire(logicalReference, wire);
            change.addWire(wire);
        }
    }

    private LogicalReference resolveLogicalReference(String referenceName, URI sourceUri, LogicalCompositeComponent parent, LogicalChange change) {
        LogicalComponent<?> source = parent.getComponent(sourceUri);
        if (source == null) {
            WireSourceNotFound error = new WireSourceNotFound(sourceUri, parent.getUri());
            change.addError(error);
            return null;
        }
        LogicalReference logicalReference;
        if (referenceName == null) {
            // a reference was not specified
            if (source.getReferences().size() == 0) {
                WireSourceNoReference error = new WireSourceNoReference(sourceUri, parent.getUri());
                change.addError(error);
                return null;
            } else if (source.getReferences().size() != 1) {
                WireSourceAmbiguousReference error = new WireSourceAmbiguousReference(sourceUri, parent.getUri());
                change.addError(error);
                return null;
            }
            // default to the only reference
            logicalReference = source.getReferences().iterator().next();
        } else {
            logicalReference = source.getReference(referenceName);
            if (logicalReference == null) {
                WireSourceReferenceNotFound error = new WireSourceReferenceNotFound(sourceUri, referenceName, parent.getUri());
                change.addError(error);
                return null;
            }
        }
        return logicalReference;
    }

    /**
     * Resolves the wire target URI to a service provided by a component in the parent composite.
     *
     * @param targetUri the atrget URI to resolve.
     * @param parent    the parent composite to resolve against
     * @param change    the logical change to report errors against
     * @return the fully resolved wire target URI
     */
    private URI resolveTargetUri(URI targetUri, LogicalCompositeComponent parent, LogicalChange change) {
        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = parent.getComponent(targetComponentUri);
        if (targetComponent == null) {
            WireTargetNotFound error = new WireTargetNotFound(targetUri, parent.getUri());
            change.addError(error);
            return null;
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                WireTargetServiceNotFound error = new WireTargetServiceNotFound(targetUri, parent.getUri());
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
                    AmbiguousWireTargetService error = new AmbiguousWireTargetService(targetUri, parent.getUri());
                    change.addError(error);
                    return null;
                }
                target = service;
            }
            if (target == null) {
                WireTargetNoService error = new WireTargetNoService(targetUri, parent.getUri());
                change.addError(error);
                return null;
            }
            return target.getUri();
        }

    }

}
