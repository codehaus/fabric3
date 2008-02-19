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
package org.fabric3.fabric.assembly.resolver;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.util.UriHelper;

/**
 * Default WireResolver implementation
 *
 * @version $Rev$ $Date$
 */
public class DefaultWireResolver implements WireResolver {

    public void resolve(LogicalComponent<?> component) throws ResolutionException {
        
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            Collection<LogicalComponent<?>> components = composite.getComponents();
            if (!components.isEmpty()) {
                for (LogicalComponent<?> child : components) {
                    resolve(child);
                }
            }
        }
        
        resolveReferences(component);
        resolveServices(component);
        
    }

    public void resolve(LogicalService service) throws ResolutionException {
        URI serviceUri = service.getUri();
        URI promotedUri = service.getPromotedUri();
        if (promotedUri == null) {
            // this service does not promote another, nothing to do
            return;
        }

        URI promotedComponentUri = UriHelper.getDefragmentedName(promotedUri);
        String promotedServiceName = promotedUri.getFragment();
        LogicalCompositeComponent composite = (LogicalCompositeComponent) service.getParent();
        LogicalComponent<?> promotedComponent = composite.getComponent(promotedComponentUri);
        if (promotedComponent == null) {
            throw new PromotedComponentNotFoundException(serviceUri, promotedComponentUri);
        }

        if (promotedServiceName == null) {
            if (promotedComponent.getServices().size() == 0) {
                throw new PromotedServiceNotFoundException(serviceUri, promotedComponentUri);
            } else if (promotedComponent.getServices().size() != 1) {
                throw new AmbiguousPromotedServiceException(serviceUri, promotedComponentUri);
            }
            LogicalService logicalService = promotedComponent.getServices().iterator().next();
            promotedUri = logicalService.getUri();
            service.setPromotedUri(promotedUri);
        } else {
            if (promotedComponent.getService(promotedUri.getFragment()) == null) {
                throw new PromotedServiceNotFoundException(serviceUri, promotedUri);
            }
        }
    }

    public void resolveReference(LogicalReference logicalReference, LogicalCompositeComponent composite)
            throws ResolutionException {
        List<URI> promotedUris = logicalReference.getPromotedUris();
        for (int i = 0; i < promotedUris.size(); i++) {
            URI promotedUri = promotedUris.get(i);
            URI componentId = UriHelper.getDefragmentedName(promotedUri);
            LogicalCompositeComponent parent = (LogicalCompositeComponent) logicalReference.getParent();
            LogicalComponent<?> promotedComponent = parent.getComponent(componentId);
            if (promotedComponent == null) {
                throw new PromotedComponentNotFoundException(logicalReference.getUri(), componentId);
            }

            String promotedReferenceName = promotedUri.getFragment();
            if (promotedReferenceName == null) {
                Collection<LogicalReference> componentReferences = promotedComponent.getReferences();
                if (componentReferences.size() == 0) {
                    throw new PromotedReferenceNotFoundException(logicalReference.getUri(), promotedUri);
                } else if (componentReferences.size() > 1) {
                    throw new AmbiguousPromotedReferenceException(logicalReference.getUri(), promotedUri);
                }
                // FIXME this seems a little fragile
                URI referenceUri = componentReferences.iterator().next().getUri();
                logicalReference.setPromotedUri(i, referenceUri);
            } else if (promotedComponent.getReference(promotedReferenceName) == null) {
                throw new PromotedReferenceNotFoundException(logicalReference.getUri(), promotedUri);
            }

        }

        LogicalComponent<?> component = logicalReference.getParent();
        ReferenceDefinition reference = logicalReference.getDefinition();
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Map<String, ComponentReference> targets = definition.getReferences();

        String referenceName = reference.getName();
        ComponentReference target = targets.get(referenceName);
        if (target == null) {
            // case where a reference is specified but not configured, e.g. promoted or autowirable
            if (!logicalReference.getBindings().isEmpty() || isPromoted(composite, component, referenceName)) {
                return;
            }
            ServiceContract<?> requiredContract = determineContract(logicalReference);

            boolean required = reference.isRequired();
            Autowire autowire = calculateAutowire(composite, component);
            if (autowire == Autowire.ON) {
                List<URI> targetUris = resolveByType(composite, component, referenceName, requiredContract);
                if (targetUris.isEmpty() && required) {
                    URI source = logicalReference.getUri();
                    throw new AutowireTargetNotFoundException("No suitable target found for " + source, source);
                }
            } else if (reference.isRequired()) {
                // check to see if the reference was a promotion
                throw new UnspecifiedTargetException("Reference target not specified", logicalReference.getUri());
            }
        } else {
            // reference element is specified
            List<URI> uris = target.getTargets();
            if (!uris.isEmpty()) {
                URI parentUri = composite.getUri();
                List<URI> resolvedUris = new ArrayList<URI>();
                for (URI uri : uris) {
                    // fully resolve URIs
                    URI resolved = parentUri.resolve(component.getUri()).resolve(uri);
                    URI targetURI = resolveByUri(logicalReference, resolved, composite);
                    resolvedUris.add(targetURI);
                }
                logicalReference.overrideTargets(resolvedUris);
                return;
            } else if (isPromoted(composite, component, referenceName)) {
                // no URIs were specified, check to see if the reference was promoted, and if so continue
                return;
            }
            if (target.isAutowire()) {
                // a reference is specified with autowire and no target is specified on it or via promition
                ServiceContract<?> requiredContract = reference.getServiceContract();
                String fragment = target.getName();
                boolean required = reference.isRequired();
                List<URI> targetUris =
                        resolveByType(component.getParent(), component, referenceName, requiredContract);
                if (targetUris.isEmpty()) {
                    // search the target compoisite
                    targetUris = resolveByType(composite, component, fragment, requiredContract);
                }
                if (targetUris.isEmpty() && required) {
                    URI source = logicalReference.getUri();
                    throw new AutowireTargetNotFoundException("No suitable target found for", source);
                }
            } else if (reference.isRequired() && reference.getBindings().size() > 0) {
                // an unbound and un-targeted reference that is required
                URI source = logicalReference.getUri();
                throw new UnspecifiedTargetException("Reference target not specified", source);
            }
        }

    }

    /**
     * Resolves service promotions for the services of the given component. Specifically, resolves promoted URIs and
     * calaculates default services if necessary.
     *
     * @param component the component to resolve service promotions.
     * @throws ResolutionException if an error occurs resolving a promotion
     */
    private void resolveServices(LogicalComponent<?> component)
            throws ResolutionException {
        for (LogicalService service : component.getServices()) {
            resolve(service);
        }
    }

    /**
     * Resolves component references
     *
     * @param component the component containing the references to resolve
     * @throws ResolutionException if an error occurs during resolution
     */
    private void resolveReferences(LogicalComponent<?> component) throws ResolutionException {
        LogicalCompositeComponent composite = component.getParent();
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        AbstractComponentType<?, ?, ?, ?> componentType = definition.getImplementation().getComponentType();
        for (ReferenceDefinition reference : componentType.getReferences().values()) {
            String referenceName = reference.getName();
            LogicalReference logicalReference = component.getReference(referenceName);
            resolveReference(logicalReference, composite);
        }
    }

    /**
     * Resolves a reference against a composite by its service contract
     *
     * @param composite        the composite component to resolve against
     * @param component        the component to resolve
     * @param referenceName    the reference name
     * @param requiredContract the required target contract
     * @return URI              the target URI or null if not found
     * @throws AutowireTargetNotFoundException
     *          if an errror resolving occurs
     * @throws AmbiguousAutowireTargetException
     *          if more than one target satisfies the type criteria
     */
    private List<URI> resolveByType(LogicalCompositeComponent composite,
                                    LogicalComponent<?> component,
                                    String referenceName,
                                    ServiceContract<?> requiredContract)
            throws AutowireTargetNotFoundException, AmbiguousAutowireTargetException {

        List<URI> candidates = new ArrayList<URI>();
        // find a suitable target, starting with components first
        for (LogicalComponent<?> child : composite.getComponents()) {
            ComponentDefinition<? extends Implementation<?>> candidate = child.getDefinition();
            Implementation<?> candidateImpl = candidate.getImplementation();
            AbstractComponentType<?, ?, ?, ?> candidateType = candidateImpl.getComponentType();
            for (ServiceDefinition service : candidateType.getServices().values()) {
                ServiceContract<?> targetContract = service.getServiceContract();
                if (targetContract == null) {
                    continue;
                }
                if (requiredContract.isAssignableFrom(targetContract)) {
                    candidates.add(URI.create(child.getUri().toString() + '#' + service.getName()));
                    break;
                }
            }
        }
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        } /*else if (candidates.size() > 1) {
            // For now, we have no other criteria to select from
            URI uri = URI.create(component.getUri().toString() + "#" + referenceName);
            throw new AmbiguousAutowireTargetException("More than one target found", uri, candidates);
        }*/
        for (URI target : candidates) {
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            logicalReference.addTargetUri(component.getUri().resolve(target));
        }
        /*targetUri = candidates.get(0);
        if (targetUri != null) {
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            logicalReference.addTargetUri(component.getUri().resolve(targetUri));
        }*/
        return candidates;
    }

    /**
     * Determines the autowire setting for a component based on the autowire hierarchy
     *
     * @param composite the component's parent
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
            // undefined in the original parent or the component is top-level, check in the target
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
     * Fully resolves a component reference to a target component service based on a URI.
     * <p/>
     * If the reference does not contain a service name, then the target component must provide a single service.
     *
     * @param reference the reference
     * @param targetUri the target URI to resolve
     * @param composite the composite the target is contained in
     * @return the fully resolved URI of the target service
     * @throws ResolutionException if the target is invalid
     */
    private URI resolveByUri(LogicalReference reference, URI targetUri, LogicalCompositeComponent composite)
            throws ResolutionException {
        URI sourceUri = reference.getUri();
        URI targetComponentUri = UriHelper.getDefragmentedName(targetUri);
        LogicalComponent<?> targetComponent = composite.getComponent(targetComponentUri);
        if (targetComponent == null) {
            throw new ComponentReferenceTargetNotFoundException(sourceUri, targetUri);
        }

        String serviceName = targetUri.getFragment();
        if (serviceName != null) {
            if (targetComponent.getService(serviceName) == null) {
                throw new ComponentReferenceTargetNotFoundException(sourceUri, targetUri);
            }
            return targetUri;
        } else {
            if (targetComponent.getServices().size() != 1) {
                if (targetComponent.getServices().size() > 1) {
                    throw new AmbiguousComponentReferenceTargetException(sourceUri, targetComponentUri);
                } else {
                    throw new ComponentReferenceTargetHasNoServicesException(sourceUri, targetUri);
                }
            }
            LogicalService targetService = targetComponent.getServices().iterator().next();
            return targetService.getUri();
        }
    }

    /**
     * Returns true if the reference was promoted in the parent.
     *
     * @param composite     the containing composite
     * @param component     the component containing the reference
     * @param referenceName the reference name
     * @return true if the reference is promoted in the parent
     */
    private boolean isPromoted(LogicalComponent<?> composite, LogicalComponent<?> component, String referenceName) {
        for (LogicalReference compositeReference : composite.getReferences()) {
            List<URI> uris = compositeReference.getPromotedUris();
            if (component.getReferences().size() == 1) {
                // special case where reference name does not need to be specified if component has one ref
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
                URI refUri = URI.create(component.getUri().toString() + "#" + referenceName);
                for (URI uri : uris) {
                    if (refUri.equals(uri)) {
                        return true;
                    }
                }

            }
        }
        return false;

    }

    /**
     * Determines the service contract for the logical composite reference, recursing down the promotion hierarchy if
     * necessary.
     *
     * @param reference the logical reference
     * @return the service contract
     */
    private ServiceContract<?> determineContract(LogicalReference reference) {
        ServiceContract<?> contract = reference.getDefinition().getServiceContract();
        if (contract != null) {
            return contract;
        }
        // it is an error if a composite reference does not promote a component reference and this should
        // be determined before wire resolution
        assert !reference.getPromotedUris().isEmpty();
        // choose the first path to recurse into since composite references that promote more than component reference
        // must have the same service contract
        URI promotes = reference.getPromotedUris().get(0);
        URI defragmented = UriHelper.getDefragmentedName(promotes);
        String promotedReferenceName = promotes.getFragment();
        LogicalCompositeComponent parent = (LogicalCompositeComponent) reference.getParent();
        LogicalComponent<?> promotedComponent = parent.getComponent(defragmented);
        LogicalReference promotedReference;
        if (promotedReferenceName == null) {
            assert promotedComponent.getReferences().size() == 1;
            promotedReference = promotedComponent.getReferences().iterator().next();
        } else {
            promotedReference = promotedComponent.getReference(promotedReferenceName);
        }
        return determineContract(promotedReference);
    }


}
