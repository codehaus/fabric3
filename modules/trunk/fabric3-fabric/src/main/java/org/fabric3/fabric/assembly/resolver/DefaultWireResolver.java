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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fabric3.fabric.assembly.ResolutionException;
import org.fabric3.fabric.assembly.UnspecifiedTargetException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ComponentReference;
import org.fabric3.spi.model.type.ServiceContract;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.fabric3.spi.util.UriHelper;

/**
 * Default WireResolver implementation
 *
 * @version $Rev$ $Date$
 */
public class DefaultWireResolver implements WireResolver {
    private Map<ServiceContract, URI> hostAutowire = new HashMap<ServiceContract, URI>();

    public void addHostUri(ServiceContract contract, URI uri) {
        hostAutowire.put(contract, uri);
    }

    public void resolve(LogicalComponent<?> targetComposite, LogicalComponent<?> component, boolean include)
            throws ResolutionException {
        if (component.getComponents().isEmpty()) {
            resolveReferences(targetComposite, component, false);
        } else {
            for (LogicalComponent<?> child : component.getComponents()) {
                // at the top level, if the operation is include, ensure target selection is done from siblings already
                // in the parent or siblings in the included composite
                if (include) {
                    if (!child.getComponents().isEmpty()) {
                        // resolve children
                        resolveInternal(targetComposite, child);
                    } else {
                        // no children, resolve references directly using include semantics
                        resolveReferences(targetComposite, child, true);
                    }
                } else {
                    // resolve children
                    resolveInternal(component, child);
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
        ComponentType<?, ?, ?> componentType = definition.getImplementation().getComponentType();
        Map<String, ComponentReference> targets = definition.getReferences();
        for (ReferenceDefinition reference : componentType.getReferences().values()) {
            URI refUri = reference.getUri();
            String referenceName = refUri.getFragment();
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            ComponentReference target = targets.get(referenceName);
            if (target == null) {
                // case where a reference is specified but not configured, e.g. promoted or autowirable
                // check for promotions first
                String baseName = UriHelper.getBaseName(component.getUri());
                // If the composite is being included search siblings for promoted referencea, else search the
                // target composite
                if (include && resolvePromotions(baseName, logicalReference, component.getParent())) {
                    return;
                } else if (resolvePromotions(baseName, logicalReference, targetComposite)) {
                    return;
                }
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
                    throw new UnspecifiedTargetException("Reference target not specified", refUri.toString());
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
     * Resolves reference targets by searching for composite references that promote it
     *
     * @param name             the name of the reference's component
     * @param logicalReference the logical reference to resolve targets for
     * @param composite        the composite to resolve against
     * @return true if promoted targets were resolved
     */
    private boolean resolvePromotions(String name, LogicalReference logicalReference, LogicalComponent<?> composite) {
        String referenceName = logicalReference.getUri().getFragment();
        boolean found = false;
        for (LogicalReference candidate : composite.getReferences()) {
            List<URI> uris = candidate.getDefinition().getPromoted();
            for (URI uri : uris) {
                if (name.equals(UriHelper.getDefragmentedNameAsString(uri))
                        && referenceName.equals(uri.getFragment())) {
                    logicalReference.addTargetUri(candidate.getUri());
                    // FIXME only works one level
                    found = true;
                    break;
                }
            }
        }
        return found;
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
     */
    private URI resolveByType(LogicalComponent<?> composite,
                              LogicalComponent component,
                              String referenceName,
                              ServiceContract requiredContract) throws AutowireTargetNotFoundException {
        // for now, attempt to match on interface, assume the class can be loaded
        Class<?> requiredInterface = requiredContract.getInterfaceClass();
        if (requiredInterface == null) {
            throw new UnsupportedOperationException("Only interfaces support for autowire");
        }
        URI targetUri = null;
        URI candidateUri = null;
        // find a suitable target, starting with components first
        for (LogicalComponent<?> child : composite.getComponents()) {
            ComponentDefinition<? extends Implementation<?>> candidate = child.getDefinition();
            Implementation<?> candidateImpl = candidate.getImplementation();
            ComponentType<?, ?, ?> candidateType = candidateImpl.getComponentType();
            for (ServiceDefinition service : candidateType.getServices().values()) {
                Class<?> serviceInterface = service.getServiceContract().getInterfaceClass();
                if (serviceInterface == null) {
                    continue;
                }
                if (requiredInterface.equals(serviceInterface)) {
                    targetUri = URI.create(candidate.getName() + service.getUri());
                    break;
                } else if (candidateUri == null && requiredInterface.isAssignableFrom(serviceInterface)) {
                    candidateUri = URI.create(candidate.getName() + service.getUri());
                }
            }
            if (targetUri != null) {
                break;
            }
        }
        if (targetUri == null) {
            targetUri = resolvePrimordial(requiredContract);
        }
        if (candidateUri != null) {
            targetUri = candidateUri;
        }
        if (targetUri != null) {
            LogicalReference logicalReference = component.getReference(referenceName);
            assert logicalReference != null;
            logicalReference.addTargetUri(component.getUri().resolve(targetUri));
        }
        return targetUri;
    }

    /**
     * Resolves a reference type against the registered primordial components
     *
     * @param contract the reference type
     * @return a URI of a service matching the type or null
     */
    private URI resolvePrimordial(ServiceContract contract) {
        Class<?> requiredClass = contract.getInterfaceClass();
        for (Map.Entry<ServiceContract, URI> entry : hostAutowire.entrySet()) {
            if (requiredClass.isAssignableFrom(entry.getKey().getInterfaceClass())) {
                return entry.getValue().resolve("#" + entry.getKey().getInterfaceName());
            }
        }
        return null;
    }

    /**
     * Determines the autowire setting for a component based on the autowire hierarchy
     *
     * @param targetComposite the component's parent
     * @param component       the component
     * @return the autowire setting
     */
    private Autowire calculateAutowire(LogicalComponent<?> targetComposite, LogicalComponent<?> component
    ) {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Autowire autowire = definition.getAutowire();
        if (autowire == Autowire.INHERITED) {
            // first check in the original parent composite definition
            if (component.getParent() != null) {
                ComponentDefinition<? extends Implementation<?>> def = component.getParent().getDefinition();
                ComponentType<?, ?, ?> type = def.getImplementation().getComponentType();
                autowire = (CompositeComponentType.class.cast(type)).getAutowire();
                if (autowire == Autowire.OFF || autowire == Autowire.ON) {
                    return autowire;
                }
            }
            // undefined in the original parent or the component is top-level, check in the target
            ComponentDefinition<? extends Implementation<?>> parentDefinition = targetComposite.getDefinition();
            ComponentType<?, ?, ?> parentType = parentDefinition.getImplementation().getComponentType();
            while (CompositeComponentType.class.isInstance(parentType)) {
                autowire = (CompositeComponentType.class.cast(parentType)).getAutowire();
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
