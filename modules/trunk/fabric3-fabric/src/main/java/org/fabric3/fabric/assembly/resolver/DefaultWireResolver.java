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
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.type.Autowire;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ReferenceTarget;
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

    public void resolve(LogicalComponent<?> parent, LogicalComponent<?> component) throws ResolutionException {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        ComponentType<?, ?, ?> type = definition.getImplementation().getComponentType();
        URI parentUri = parent.getUri();
        if (CompositeComponentType.class.isInstance(type)) {
            for (LogicalComponent<?> child : component.getComponents()) {
                resolve(component, child); // recurse decendents
            }
        } else {
            // a leaf level component
            ComponentType<?, ?, ?> componentType = definition.getImplementation().getComponentType();
            Map<String, ReferenceTarget> targets = definition.getReferenceTargets();
            for (ReferenceDefinition reference : componentType.getReferences().values()) {
                URI refUri = reference.getUri();
                String referenceName = refUri.getFragment();
                LogicalReference logicalReference = component.getReference(referenceName);
                assert logicalReference != null;
                ReferenceTarget target = targets.get(referenceName);
                if (target == null) {
                    // case where a reference is specified but not configured, e.g. promoted or autowirable
                    // check for promotions first
                    String baseName = UriHelper.getBaseName(component.getUri());
                    boolean found = false;
                    for (LogicalReference candidate : parent.getReferences()) {
                        List<URI> uris = candidate.getDefinition().getPromoted();
                        for (URI uri : uris) {
                            if (baseName.equals(UriHelper.getDefragmentedNameAsString(uri))
                                    && referenceName.equals(uri.getFragment())) {
                                logicalReference.addTargetUri(candidate.getUri());
                                // FIXME only works one level
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            return;
                        }
                    }
                    ///////////////
                    ServiceContract requiredContract = reference.getServiceContract();
                    boolean required = reference.isRequired();
                    Autowire autowire = definition.getAutowire();
                    if (autowire == Autowire.INHERITED) {
                        // FIXME should we recurse all the way to the domain for autowire?
                        ComponentDefinition<? extends Implementation<?>> parentDefinition = parent.getDefinition();
                        ComponentType<?, ?, ?> parentType = parentDefinition.getImplementation().getComponentType();
                        if (CompositeComponentType.class.isInstance(parentType)) {
                            autowire = (CompositeComponentType.class.cast(parentType)).getAutowire();
                        } else {
                            autowire = Autowire.OFF;
                        }
                    }
                    if (autowire == Autowire.ON) {
                        resolve(parent, component, referenceName, requiredContract, required);
                    } else {
                        throw new UnspecifiedTargetException("Reference target not specified", refUri.toString());
                    }
                } else {
                    List<URI> uris = target.getTargets();
                    if (!uris.isEmpty()) {
                        for (URI uri : uris) {
                            // fully resolve URIs
                            logicalReference.addTargetUri(parentUri.resolve(component.getUri()).resolve(uri));
                        }
                        continue;
                    }

                    if (target.isAutowire()) {
                        ServiceContract requiredContract = reference.getServiceContract();
                        String fragment = target.getReferenceName().getFragment();
                        boolean required = reference.isRequired();
                        resolve(parent, component, fragment, requiredContract, required);
                    }
                }
            }
        }
    }

    public void addHostUri(ServiceContract contract, URI uri) {
        hostAutowire.put(contract, uri);
    }

    /**
     * Performs the actual resolution against a composite
     *
     * @param composite        the composite component to resolve against
     * @param component        the component to resolve
     * @param referenceName    the reference name
     * @param requiredContract the required target contract
     * @param required         true if the autowire is required
     * @throws AutowireTargetNotFoundException
     *          if an errror resolving occurs
     */
    private void resolve(LogicalComponent<?> composite,
                         LogicalComponent component,
                         String referenceName,
                         ServiceContract requiredContract,
                         boolean required) throws AutowireTargetNotFoundException {
        // for now, attempt to match on interface, assume the class can be loaded
        Class<?> requiredInterface = requiredContract.getInterfaceClass();
        if (requiredInterface == null) {
            throw new UnsupportedOperationException("Only interfaces support for autowire");
        }
        // autowire to a target in the parent
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
        if (targetUri == null && required) {
            String fullRef = component.getUri().toString() + "#" + referenceName;
            throw new AutowireTargetNotFoundException("No suitable target found for", fullRef);
        }
    }

    private URI resolvePrimordial(ServiceContract contract) {
        Class<?> requiredClass = contract.getInterfaceClass();
        for (Map.Entry<ServiceContract, URI> entry : hostAutowire.entrySet()) {
            if (requiredClass.isAssignableFrom(entry.getKey().getInterfaceClass())) {
                return entry.getValue().resolve("#" + entry.getKey().getInterfaceName());
            }
        }
        return null;
    }
}
