/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.model.logical;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.scdl.Autowire;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Property;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.wire.WiringService;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

/**
 * @version $Revision$ $Date$
 */
public class LogicalModelGeneratorImpl implements LogicalModelGenerator {

    private final WiringService wiringService;
    private final PromotionNormalizer promotionNormalizer;
    private final LogicalComponentManager logicalComponentManager;
    private final ComponentInstantiator atomicComponentInstantiator;
    private final ComponentInstantiator compositeComponentInstantiator;

    public LogicalModelGeneratorImpl(@Reference WiringService wiringService,
                                     @Reference PromotionNormalizer promotionNormalizer,
                                     @Reference LogicalComponentManager logicalComponentManager,
                                     @Reference(name = "atomicComponentInstantiator")ComponentInstantiator atomicComponentInstantiator,
                                     @Reference(name = "compositeComponentInstantiator")ComponentInstantiator compositeComponentInstantiator) {
        this.wiringService = wiringService;
        this.promotionNormalizer = promotionNormalizer;
        this.logicalComponentManager = logicalComponentManager;
        this.atomicComponentInstantiator = atomicComponentInstantiator;
        this.compositeComponentInstantiator = compositeComponentInstantiator;
    }

    @SuppressWarnings("unchecked")
    public void include(LogicalCompositeComponent parent, Composite composite) throws ActivateException {

        // merge the property values into the parent
        for (Property property : composite.getProperties().values()) {
            String name = property.getName();
            if (parent.getPropertyValues().containsKey(name)) {
                throw new ActivateException("Duplicate property", name);
            }
            Document value = property.getDefaultValue();
            parent.setPropertyValue(name, value);
        }

        // instantiate all the components in the composite and add them to the parent
        String base = parent.getUri().toString();
        Collection<ComponentDefinition<? extends Implementation<?>>> definitions = composite.getComponents().values();
        List<LogicalComponent<?>> components = new ArrayList<LogicalComponent<?>>(definitions.size());

        instantiateComponents(parent, composite, definitions, components);

        List<LogicalService> services = instantiateServices(parent, composite, base);
        List<LogicalReference> references = instantiateReferences(parent, composite, base);

        resolveWires(parent.getComponents(), services, references);

        // normalize bindings for each new component
        for (LogicalComponent<?> component : components) {
            normalize(component);
        }

    }

    @SuppressWarnings("unchecked")
    private LogicalComponent<?> instantiate(LogicalCompositeComponent parent, ComponentDefinition<?> definition) throws InstantiationException {

        Implementation<?> impl = definition.getImplementation();
        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(impl.getType())) {
            return compositeComponentInstantiator.instantiate(parent, (ComponentDefinition<CompositeImplementation>) definition);
        } else {
            return atomicComponentInstantiator.instantiate(parent, (ComponentDefinition<Implementation<?>>) definition);
        }

    }

    private void resolveWires(Collection<LogicalComponent<?>> components, List<LogicalService> services, List<LogicalReference> references)
            throws ActivateException {

        // resolve wires for composite services merged into the domain
        try {
            for (LogicalService service : services) {
                wiringService.promote(service);
            }
        } catch (WiringException e) {
            throw new ActivateException(e);
        }

        // resove composite references merged into the domain
        for (LogicalReference reference : references) {
            try {
                wiringService.wire(reference, logicalComponentManager.getDomain());
            } catch (WiringException e) {
                throw new ActivateException(e);
            }
        }

        // resolve wires for each new component
        try {
            for (LogicalComponent<?> component : components) {
                wiringService.wire(component);
            }
        } catch (WiringException e) {
            throw new ActivateException(e);
        }

    }

    private List<LogicalReference> instantiateReferences(LogicalComponent<CompositeImplementation> parent, Composite composite, String base) {

        // merge the composite reference definitions into the parent
        List<LogicalReference> references = new ArrayList<LogicalReference>(composite.getReferences().size());
        for (CompositeReference compositeReference : composite.getReferences().values()) {
            URI referenceURi = URI.create(base + '#' + compositeReference.getName());
            LogicalReference logicalReference = new LogicalReference(referenceURi, compositeReference, parent);
            for (URI promotedUri : compositeReference.getPromotedUris()) {
                URI resolvedUri = URI.create(base + "/" + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }
            for (BindingDefinition binding : compositeReference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            for (BindingDefinition binding : compositeReference.getCallbackBindings()) {
                logicalReference.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            references.add(logicalReference);
            parent.addReference(logicalReference);
        }
        return references;

    }

    private List<LogicalService> instantiateServices(LogicalComponent<CompositeImplementation> parent, Composite composite, String base) {
        List<LogicalService> services = new ArrayList<LogicalService>();
        // merge the composite service declarations into the parent
        for (CompositeService compositeService : composite.getServices().values()) {
            URI serviceURI = URI.create(base + '#' + compositeService.getName());
            URI promotedURI = compositeService.getPromote();
            LogicalService logicalService = new LogicalService(serviceURI, compositeService, parent);
            logicalService.setPromotedUri(URI.create(base + "/" + promotedURI));
            for (BindingDefinition binding : compositeService.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }
            for (BindingDefinition binding : compositeService.getCallbackBindings()) {
                logicalService.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }
            services.add(logicalService);
            parent.addService(logicalService);
        }
        return services;

    }

    private void instantiateComponents(LogicalCompositeComponent parent,
                                       Composite composite,
                                       Collection<ComponentDefinition<? extends Implementation<?>>> definitions,
                                       List<LogicalComponent<?>> components) throws InstantiationException {

        for (ComponentDefinition<? extends Implementation<?>> definition : definitions) {
            LogicalComponent<?> logicalComponent = instantiate(parent, definition);
            // use autowire settings on the original composite as an override if they are not specified on the component
            Autowire autowire;
            if (definition.getAutowire() == Autowire.INHERITED) {
                autowire = composite.getAutowire();
            } else {
                autowire = definition.getAutowire();
            }
            if (autowire == Autowire.ON || autowire == Autowire.OFF) {
                logicalComponent.setAutowireOverride(autowire);
            }
            components.add(logicalComponent);
            parent.addComponent(logicalComponent);
        }

    }

    /**
     * Normalizes the component and any children
     *
     * @param component the component to normalize
     */
    private void normalize(LogicalComponent<?> component) {

        Implementation<?> implementation = component.getDefinition().getImplementation();

        if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(implementation.getType())) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                normalize(child);
            }
        } else {
            promotionNormalizer.normalize(component);
        }

    }

}
