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
package org.fabric3.fabric.instantiator.resolve;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.fabric.instantiator.resolve.TargetResolutionException;
import org.fabric3.fabric.instantiator.resolve.TargetResolutionService;

/**
 * Resolution based on an explicitly requested target.
 *
 * @version $Revsion$ $Date$
 */
public class TypeBasedAutoWireService implements TargetResolutionService {

    public void resolve(LogicalReference logicalReference, LogicalCompositeComponent compositeComponent) throws TargetResolutionException {

        ComponentReference componentReference = logicalReference.getComponentReference();
        LogicalComponent<?> component = logicalReference.getParent();

        if (componentReference == null) {
            // this is a reference on an atomic component, not a composite reference
            if (!logicalReference.getBindings().isEmpty() || isPromoted(compositeComponent, logicalReference)) {
                return;
            }
            ServiceContract<?> requiredContract = determineContract(logicalReference);

            Autowire autowire = calculateAutowire(compositeComponent, component);
            if (autowire == Autowire.ON) {
                resolveByType(compositeComponent, component, logicalReference, requiredContract);
            }

        } else {
            // the reference is a composite
            List<URI> uris = componentReference.getTargets();
            if (!uris.isEmpty() || isPromoted(compositeComponent, logicalReference)) {
                return;
            }

            if (componentReference.isAutowire()) {
                ReferenceDefinition referenceDefinition = logicalReference.getDefinition();
                ServiceContract<?> requiredContract = referenceDefinition.getServiceContract();
                boolean resolved = resolveByType(component.getParent(), component, logicalReference, requiredContract);
                if (!resolved) {
                    resolveByType(compositeComponent, component, logicalReference, requiredContract);
                }
            }
        }

        if (logicalReference.getWires().isEmpty() && logicalReference.getDefinition().isRequired() && logicalReference.getBindings().isEmpty()) {
            throw new TargetResolutionException("Unable to resolve reference " + logicalReference.getUri());
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
     * Attempts to resolve a reference against a composite using the autowire matching algorithm. If the reference is resolved, the target URI (or
     * URIs) is added to the reference.
     *
     * @param composite        the composite to resolve against
     * @param component        the component containing the reference
     * @param logicalReference the logical reference
     * @param contract         the contract to match against
     * @return true if the reference has been resolved.
     * @throws org.fabric3.fabric.instantiator.resolve.TargetResolutionException if an error occurs during resolution
     */
    private boolean resolveByType(LogicalCompositeComponent composite,
                                  LogicalComponent<?> component,
                                  LogicalReference logicalReference,
                                  ServiceContract<?> contract) throws TargetResolutionException {

        List<URI> candidates = new ArrayList<URI>();

        for (LogicalComponent<?> child : composite.getComponents()) {
            for (LogicalService service : child.getServices()) {
                ServiceContract<?> targetContract = determineContract(service);
                if (targetContract == null) {
                    // This is a programming error since a non-composite service must have a service contract
                    throw new ServiceContractNotFoundException("No service contract specified on service: " + service.getUri());
                }
                if (contract.isAssignableFrom(targetContract)) {
                    candidates.add(service.getUri());
                    break;
                }
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        for (URI target : candidates) {
            logicalReference.addTargetUri(component.getUri().resolve(target));
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

    /**
     * Determines the service contract for a promoted reference
     *
     * @param reference the reference to determine the contract for
     * @return the service contract
     */
    private ServiceContract<?> determineContract(LogicalReference reference) {

        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }

        assert !reference.getPromotedUris().isEmpty();

        URI promotes = reference.getPromotedUris().get(0);
        URI defragmented = UriHelper.getDefragmentedName(promotes);
        String promotedReferenceName = promotes.getFragment();
        LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
        LogicalComponent<?> promotedComponent = parent.getComponent(defragmented);
        LogicalReference promotedReference;

        if (promotedReferenceName == null) {
            // if the component has only one reference, the SCA specification allows it to be used as a default
            assert promotedComponent.getReferences().size() == 1;
            promotedReference = promotedComponent.getReferences().iterator().next();
        } else {
            promotedReference = promotedComponent.getReference(promotedReferenceName);
        }

        return determineContract(promotedReference);

    }

    /**
     * Returns the service contract for a service. Promoted services often do not specify a service contract explicitly, instead using a service
     * contract defined further down in the promotion hieratchy. In these cases, the service contract is often inferred from the implementation (e.g.
     * a Java class) or explicitly declared within the component definition in a composite file.
     *
     * @param service the composite service to determine the service contract for.
     * @return the service contract or null if none is found
     */
    private ServiceContract<?> determineContract(LogicalService service) {
        ServiceContract<?> contract = service.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        if (!(service.getParent() instanceof LogicalCompositeComponent)) {
            return null;
        }
        LogicalCompositeComponent parent = (LogicalCompositeComponent) service.getParent();
        URI promotedUri = service.getPromotedUri();
        LogicalComponent<?> promoted = parent.getComponent(UriHelper.getDefragmentedName(promotedUri));
        assert promoted != null;
        String serviceName = promotedUri.getFragment();
        LogicalService promotedService;
        if (serviceName == null && promoted.getServices().size() == 1) {
            // select the default service as a service name was not specified
            Collection<LogicalService> services = promoted.getServices();
            promotedService = services.iterator().next();
        } else if (serviceName == null) {
            // programing error
            throw new AssertionError("Service must be specified");
        } else {
            promotedService = promoted.getService(serviceName);
        }
        assert promotedService != null;
        contract = promotedService.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        } else {
            // this is another promoted service, so recurse further into the promotion hierarchy
            contract = determineContract(promotedService);
        }
        return contract;
    }
}
