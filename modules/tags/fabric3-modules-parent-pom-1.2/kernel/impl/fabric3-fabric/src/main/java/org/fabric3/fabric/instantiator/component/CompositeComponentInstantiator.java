/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.fabric.instantiator.component;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.fabric.instantiator.ComponentInstantiator;
import org.fabric3.fabric.instantiator.InstantiationContext;
import org.fabric3.fabric.instantiator.WireInstantiator;
import org.fabric3.fabric.documentloader.DocumentLoader;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.ComponentReference;
import org.fabric3.model.type.component.ComponentService;
import org.fabric3.model.type.component.Composite;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.CompositeReference;
import org.fabric3.model.type.component.CompositeService;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalWire;

/**
 * Instatiates a composite component in the logical representation of a domain. Child components will be recursively instantiated if they exist.
 *
 * @version $Rev$ $Date$
 */
public class CompositeComponentInstantiator extends AbstractComponentInstantiator {

    private ComponentInstantiator atomicComponentInstantiator;
    private WireInstantiator wireInstantiator;

    public CompositeComponentInstantiator(
            @Reference(name = "atomicComponentInstantiator") ComponentInstantiator atomicComponentInstantiator,
            @Reference WireInstantiator wireInstantiator,
            @Reference(name = "documentLoader") DocumentLoader documentLoader) {
        super(documentLoader);
        this.atomicComponentInstantiator = atomicComponentInstantiator;
        this.wireInstantiator = wireInstantiator;
    }

    @SuppressWarnings("unchecked")
    public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent,
                                                                         Map<String, Document> properties,
                                                                         ComponentDefinition<I> definition,
                                                                         InstantiationContext context) {
        ComponentDefinition<CompositeImplementation> def = (ComponentDefinition<CompositeImplementation>) definition;
        return LogicalComponent.class.cast(instantiateComposite(parent, properties, def, context));
    }

    private LogicalCompositeComponent instantiateComposite(LogicalCompositeComponent parent,
                                                           Map<String, Document> properties,
                                                           ComponentDefinition<CompositeImplementation> definition,
                                                           InstantiationContext context) {

        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        Composite composite = definition.getImplementation().getComponentType();

        LogicalCompositeComponent component = new LogicalCompositeComponent(uri, definition, parent);
        initializeProperties(component, definition, context);
        instantiateChildComponents(component, properties, composite, context);
        instantiateCompositeServices(component, composite);
        instantiateCompositeReferences(parent, component, composite);
        wireInstantiator.instantiateWires(composite, component, context);
        return component;

    }

    private void instantiateChildComponents(LogicalCompositeComponent parent,
                                            Map<String, Document> properties,
                                            Composite composite,
                                            InstantiationContext context) {

        // create the child components
        for (ComponentDefinition<? extends Implementation<?>> child : composite.getDeclaredComponents().values()) {

            LogicalComponent<?> childComponent;
            if (child.getImplementation().isComposite()) {
                childComponent = instantiate(parent, properties, child, context);
            } else {
                childComponent = atomicComponentInstantiator.instantiate(parent, properties, child, context);
            }
            parent.addComponent(childComponent);
        }

    }

    private void instantiateCompositeServices(LogicalCompositeComponent component, Composite composite) {

        ComponentDefinition<CompositeImplementation> definition = component.getDefinition();
        String uriBase = component.getUri().toString() + "/";

        for (CompositeService service : composite.getServices().values()) {

            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            logicalService.setPromotedUri(URI.create(uriBase + service.getPromote()));

            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }

            for (BindingDefinition binding : service.getCallbackBindings()) {
                logicalService.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }

            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                // Merge/override logical reference configuration created above with service configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // service configuration. This information must be merged with or used to override any
                // configuration that was created by service promotions within the composite
                if (!componentService.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentService.getBindings()) {
                        bindings.add(new LogicalBinding<BindingDefinition>(binding, logicalService));
                    }
                    logicalService.overrideBindings(bindings);
                }
            }

            component.addService(logicalService);

        }

    }

    private void instantiateCompositeReferences(LogicalCompositeComponent parent,
                                                LogicalCompositeComponent component,
                                                Composite composite) {

        ComponentDefinition<CompositeImplementation> definition = component.getDefinition();
        String uriBase = component.getUri().toString() + "/";

        // create logical references based on promoted references in the composite definition
        for (CompositeReference reference : composite.getReferences().values()) {

            String name = reference.getName();
            URI referenceUri = component.getUri().resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);

            for (BindingDefinition binding : reference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }

            for (BindingDefinition binding : reference.getCallbackBindings()) {
                logicalReference.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }

            for (URI promotedUri : reference.getPromotedUris()) {
                URI resolvedUri = URI.create(uriBase + promotedUri.toString());
                logicalReference.addPromotedUri(resolvedUri);
            }

            ComponentReference componentReference = definition.getReferences().get(name);

            if (componentReference != null) {

                // Merge/override logical reference configuration created above with reference configuration on the
                // composite use. For example, when the component is used as an implementation, it may contain
                // reference configuration. This information must be merged with or used to override any
                // configuration that was created by reference promotions within the composite
                if (!componentReference.getBindings().isEmpty()) {
                    List<LogicalBinding<?>> bindings = new ArrayList<LogicalBinding<?>>();
                    for (BindingDefinition binding : componentReference.getBindings()) {
                        bindings.add(new LogicalBinding<BindingDefinition>(binding, logicalReference));
                    }
                    logicalReference.overrideBindings(bindings);
                }

                if (!componentReference.getTargets().isEmpty()) {
                    List<URI> targets = new ArrayList<URI>();
                    for (URI targetUri : componentReference.getTargets()) {
                        // the target is relative to the component's parent, not the component
                        targets.add(URI.create(parent.getUri().toString() + "/" + targetUri));
                    }
                    // xcv potentially remove if LogicalWires added to LogicalReference
                    LogicalCompositeComponent grandParent = parent.getParent();
                    Set<LogicalWire> wires = new LinkedHashSet<LogicalWire>();
                    if (null != grandParent) {
                        for (URI targetUri : targets) {
                            LogicalWire wire = new LogicalWire(grandParent, logicalReference, targetUri);
                            wires.add(wire);
                        }
                        grandParent.overrideWires(logicalReference, wires);
                    } else {
                        for (URI targetUri : targets) {
                            LogicalWire wire = new LogicalWire(parent, logicalReference, targetUri);
                            wires.add(wire);
                        }
                        parent.overrideWires(logicalReference, wires);
                    }
                    // end remove
                }

            }

            component.addReference(logicalReference);

        }
    }


}
