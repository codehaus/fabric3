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
package org.fabric3.fabric.assembly;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.assembly.resolver.WireResolver;
import org.fabric3.fabric.assembly.store.AssemblyStore;
import org.fabric3.fabric.runtime.ComponentNames;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.Referenceable;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.util.UriHelper;

/**
 * The default RuntimeAssembly implementation
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class RuntimeAssemblyImpl extends AbstractAssembly implements RuntimeAssembly {
    private Map<URI, LogicalComponent<?>> hostComponents;

    public RuntimeAssemblyImpl(@Reference GeneratorRegistry generatorRegistry,
                               @Reference WireResolver wireResolver,
                               @Reference PromotionNormalizer normalizer,
                               @Reference Allocator allocator,
                               @Reference RoutingService routingService,
                               @Reference AssemblyStore store) {
        super(ComponentNames.RUNTIME_URI,
              generatorRegistry,
              wireResolver,
              normalizer,
              allocator,
              routingService,
              store,
              null);
        this.hostComponents = new HashMap<URI, LogicalComponent<?>>();
    }

    public void instantiateHostComponentDefinition(URI uri, ComponentDefinition<?> definition)
            throws InstantiationException {
        LogicalComponent<?> component = instantiate(domainUri, domain, definition);
        hostComponents.put(uri, component);
    }

    @Override
    protected Referenceable resolveTarget(URI uri, List<LogicalComponent<CompositeImplementation>> components)
            throws ResolutionException {
        // TODO only resolves one level deep
        for (LogicalComponent<CompositeImplementation> component : components) {
            URI defragmentedUri = UriHelper.getDefragmentedName(uri);
            Referenceable target = component.getComponent(defragmentedUri);
            if (target != null) {
                return target;
            }
            target = component.getReference(uri.getFragment());
            if (target != null) {
                return target;
            }
            target = hostComponents.get(defragmentedUri);
            if (target != null) {
                return target;
            }
        }
        throw new TargetNotFoundException("Target not found", uri.toString());
    }


}
