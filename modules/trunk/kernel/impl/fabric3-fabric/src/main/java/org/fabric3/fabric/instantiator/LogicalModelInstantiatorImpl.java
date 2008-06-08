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
package org.fabric3.fabric.instantiator;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.fabric.instantiator.normalize.PromotionNormalizer;
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
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.util.UriHelper;

/**
 * @version $Revision$ $Date$
 */
public class LogicalModelInstantiatorImpl implements LogicalModelInstantiator {

    private final WiringService wiringService;
    private final PromotionNormalizer promotionNormalizer;
    private final LogicalComponentManager logicalComponentManager;
    private final ComponentInstantiator atomicComponentInstantiator;
    private final ComponentInstantiator compositeComponentInstantiator;


    public LogicalModelInstantiatorImpl(@Reference WiringService wiringService,
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
    public LogicalChange include(LogicalCompositeComponent parent, Composite composite) throws ActivateException {

        LogicalChange change = new LogicalChange(parent);

        // merge the property values into the parent
        Map<String, Document> properties = includeProperties(parent, composite);

        // instantiate all the components in the composite and add them to the parent
        List<LogicalComponent<?>> newComponents = instantiateComponents(parent, properties, composite);
        List<LogicalService> services = instantiateServices(parent, composite);
        List<LogicalReference> references = instantiateReferences(parent, composite);

        resolveWires(parent.getComponents(), services, references);

        // normalize bindings for each new component
        for (LogicalComponent<?> component : newComponents) {
            normalize(component);
        }
        return change;
    }

    public LogicalChange exclude(LogicalCompositeComponent parent, Composite composite) throws ActivateException {
        LogicalChange change = new LogicalChange(parent);
        // merge the property values into the parent
        excludeProperties(parent, composite, change);
        //merge the component values into the parent
        excludeComponents(parent, composite, change);
        //merge the service values into the parent
        excludeServices(parent, composite, change);
        return change;
    }

    private Map<String, Document> includeProperties(LogicalCompositeComponent parent, Composite composite) throws ActivateException {
        for (Property property : composite.getProperties().values()) {
            String name = property.getName();
            if (parent.getPropertyValues().containsKey(name)) {
                throw new ActivateException("Duplicate property", name);
            }
            parent.setPropertyValue(name, property.getDefaultValue());
        }
        return parent.getPropertyValues();
    }

    private Map<String, Document> excludeProperties(LogicalCompositeComponent parent, Composite composite, LogicalChange change) {
        Map<String, Document> map = parent.getPropertyValues();
        for (Property property : composite.getProperties().values()) {
            String name = property.getName();
            change.removeProperty(name);
        }
        return map;
    }

    private void excludeComponents(LogicalCompositeComponent parent, Composite composite, LogicalChange change) {
        Set<String> keys = composite.getComponents().keySet();
        for (String key : keys) {
            List<LogicalComponent<?>> list = parent.getComponents();
            for (LogicalComponent<?> component : list) {
                URI uri = component.getUri();
                if (UriHelper.getBaseName(uri).equals(key)) {
                    change.removeComponent(component);
                    parent.removeComponent(uri);
                }
            }
        }
    }

    private List<LogicalComponent<?>> instantiateComponents(LogicalCompositeComponent parent,
                                                            Map<String, Document> properties,
                                                            Composite composite) throws InstantiationException {

        Collection<ComponentDefinition<? extends Implementation<?>>> definitions = composite.getComponents().values();
        List<LogicalComponent<?>> newComponents = new ArrayList<LogicalComponent<?>>(definitions.size());
        for (ComponentDefinition<? extends Implementation<?>> definition : definitions) {
            LogicalComponent<?> logicalComponent = instantiate(parent, properties, definition);
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
            newComponents.add(logicalComponent);
            parent.addComponent(logicalComponent);
        }
        return newComponents;
    }

    @SuppressWarnings("unchecked")
    private LogicalComponent<?> instantiate(LogicalCompositeComponent parent,
                                            Map<String, Document> properties,
                                            ComponentDefinition<?> definition) throws InstantiationException {

        if (definition.getImplementation().isComposite()) {
            return compositeComponentInstantiator.instantiate(parent, properties, (ComponentDefinition<CompositeImplementation>) definition);
        } else {
            return atomicComponentInstantiator.instantiate(parent, properties, (ComponentDefinition<Implementation<?>>) definition);
        }

    }

    private List<LogicalService> instantiateServices(LogicalComponent<CompositeImplementation> parent, Composite composite) {
        String base = parent.getUri().toString();
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

    private void excludeServices(LogicalComponent<CompositeImplementation> parent, Composite composite, LogicalChange change) {
        String base = parent.getUri().toString();
        // merge the composite service declarations into the parent
        for (CompositeService compositeService : composite.getServices().values()) {
            URI serviceURI = URI.create(base + '#' + compositeService.getName());
            change.removeService(serviceURI);

        }

    }

    private List<LogicalReference> instantiateReferences(LogicalComponent<CompositeImplementation> parent, Composite composite) {
        String base = parent.getUri().toString();
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

    private void resolveWires(Collection<LogicalComponent<?>> components, List<LogicalService> services, List<LogicalReference> references)
            throws ActivateException {

        // resolve wires for composite services merged into the domain
        for (LogicalService service : services) {
            wiringService.promote(service);
        }

        // resove composite references merged into the domain
        for (LogicalReference reference : references) {
            wiringService.wire(reference, logicalComponentManager.getDomain());
        }

        // resolve wires for each new component
        for (LogicalComponent<?> component : components) {
            wiringService.wire(component);
        }

    }

    /**
     * Normalizes the component and any children
     *
     * @param component the component to normalize
     */
    private void normalize(LogicalComponent<?> component) {
        if (component instanceof LogicalCompositeComponent) {
            LogicalCompositeComponent composite = (LogicalCompositeComponent) component;
            for (LogicalComponent<?> child : composite.getComponents()) {
                normalize(child);
            }
        } else {
            promotionNormalizer.normalize(component);
        }

    }

}
