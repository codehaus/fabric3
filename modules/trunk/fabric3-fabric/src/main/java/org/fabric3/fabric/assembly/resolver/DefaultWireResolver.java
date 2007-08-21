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
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.assembly.ResolutionException;
import org.fabric3.fabric.assembly.UnspecifiedTargetException;
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
import org.fabric3.spi.model.instance.LogicalReference;

/**
 * Default WireResolver implementation
 *
 * @version $Rev$ $Date$
 */
public class DefaultWireResolver implements WireResolver {

    public void resolve(LogicalComponent<?> targetComposite, LogicalComponent<?> component)
            throws ResolutionException {
        if (component.getComponents().isEmpty()) {
            resolveReferences(targetComposite, component, false);
        } else {
            for (LogicalComponent<?> child : component.getComponents()) {
                if (!child.getComponents().isEmpty()) {
                    // resolve children
                    resolveInternal(targetComposite, child);
                } else {
                    // no children, resolve references directly using include semantics
                    resolveReferences(targetComposite, child, true);
                }
            }
        }
    }

    private void resolveInternal(LogicalComponent<?> targetComposite, LogicalComponent<?> component)
            throws ResolutionException {
        if (component.getComponents().isEmpty()) {
            resolveReferences(targetComposite, component, false);
        } else {
            for (LogicalComponent<?> child : component.getComponents()) {
                // resolve children
                resolveInternal(component, child);
            }
        }
    }

    /**
     * Resolves component references
     *
     * @param targetComposite the target parent component
     * @param component       the component containing the references to resolve
     * @param include         if true, the component's parent
     * @throws ResolutionException if an error occurs during resolution
     */
    private void resolveReferences(LogicalComponent<?> targetComposite, LogicalComponent<?> component, boolean include)
            throws ResolutionException {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        AbstractComponentType<?, ?, ?> componentType = definition.getImplementation().getComponentType();
        Map<String, ComponentReference> targets = definition.getReferences();
        for (ReferenceDefinition reference : componentType.getReferences().values()) {
            String referenceName = reference.getName();
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            ComponentReference target = targets.get(referenceName);
            if (target == null) {
                // case where a reference is specified but not configured, e.g. promoted or autowirable
                ServiceContract requiredContract = reference.getServiceContract();
                boolean required = reference.isRequired();
                Autowire autowire = calculateAutowire(targetComposite, component);
                if (autowire == Autowire.ON) {
                    URI targetUri = null;
                    if (include) {
                        // for an include, search the siblings prior to the target composite
                        targetUri = resolveByType(component.getParent(), component, referenceName, requiredContract);
                    }
                    if (targetUri == null) {
                        targetUri = resolveByType(targetComposite, component, referenceName, requiredContract);
                    }
                    if (targetUri == null && required) {
                        String fullRef = component.getUri().toString() + "#" + referenceName;
                        throw new AutowireTargetNotFoundException("No suitable target found for", fullRef);
                    }
                } else {
                    String fullRef = component.getUri().toString() + "#" + referenceName;
                    throw new UnspecifiedTargetException("Reference target not specified", fullRef);
                }
            } else {
                // reference element is specified
                List<URI> uris = target.getTargets();
                if (!uris.isEmpty()) {
                    URI parentUri = targetComposite.getUri();
                    for (URI uri : uris) {
                        // fully resolve URIs
                        logicalReference.addTargetUri(parentUri.resolve(component.getUri()).resolve(uri));
                    }
                    continue;
                }

                if (target.isAutowire()) {
                    // a reference element is specified with autowire and no target
                    ServiceContract requiredContract = reference.getServiceContract();
                    String fragment = target.getName();
                    boolean required = reference.isRequired();
                    URI targetUri = null;
                    if (include) {
                        // for an include, search the siblings prior to the target composite
                        targetUri = resolveByType(component.getParent(), component, referenceName, requiredContract);
                    }
                    if (targetUri == null) {
                        // search the target compoisite
                        targetUri = resolveByType(targetComposite, component, fragment, requiredContract);
                    }
                    if (targetUri == null && required) {
                        String fullRef = component.getUri().toString() + "#" + referenceName;
                        throw new AutowireTargetNotFoundException("No suitable target found for", fullRef);
                    }
                }
            }
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
    private URI resolveByType(LogicalComponent<?> composite,
                              LogicalComponent component,
                              String referenceName,
                              ServiceContract requiredContract)
            throws AutowireTargetNotFoundException, AmbiguousAutowireTargetException {
        URI targetUri;
        List<URI> candidates = new ArrayList<URI>();
        // find a suitable target, starting with components first
        for (LogicalComponent<?> child : composite.getComponents()) {
            ComponentDefinition<? extends Implementation<?>> candidate = child.getDefinition();
            Implementation<?> candidateImpl = candidate.getImplementation();
            AbstractComponentType<?, ?, ?> candidateType = candidateImpl.getComponentType();
            for (ServiceDefinition service : candidateType.getServices().values()) {
                ServiceContract targetContract = service.getServiceContract();
                if (targetContract == null) {
                    continue;
                }
                if (requiredContract.isAssignableFrom(targetContract)) {
                    candidates.add(URI.create(candidate.getName() + '#' + service.getName()));
                    break;
                }
            }
        }
        if (candidates.isEmpty()) {
            return null;
        } else if (candidates.size() > 1) {
            // For now, we have no other criteria to select from
            throw new AmbiguousAutowireTargetException(component.getUri().toString(), referenceName);
        }
        targetUri = candidates.get(0);
        if (targetUri != null) {
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            logicalReference.addTargetUri(component.getUri().resolve(targetUri));
        }
        return targetUri;
    }

    /**
     * Determines the autowire setting for a component based on the autowire hierarchy
     *
     * @param targetComposite the component's parent
     * @param component       the component
     * @return the autowire setting
     */
    private Autowire calculateAutowire(LogicalComponent<?> targetComposite, LogicalComponent<?> component) {
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
                AbstractComponentType<?, ?, ?> type = def.getImplementation().getComponentType();
                autowire = (Composite.class.cast(type)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    return autowire;
                }
            }
            // undefined in the original parent or the component is top-level, check in the target
            ComponentDefinition<? extends Implementation<?>> parentDefinition = targetComposite.getDefinition();
            AbstractComponentType<?, ?, ?> parentType = parentDefinition.getImplementation().getComponentType();
            while (Composite.class.isInstance(parentType)) {
                autowire = (Composite.class.cast(parentType)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    break;
                }
                targetComposite = targetComposite.getParent();
                if (targetComposite == null) {
                    break;
                }
                parentDefinition = targetComposite.getDefinition();
                parentType = parentDefinition.getImplementation().getComponentType();
            }
        }
        return autowire;
    }

}
