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
import java.util.List;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class CompositeComponentInstantiator extends AbstractComponentInstantiator<CompositeImplementation> {
    
    private ComponentInstantiator<Implementation<?>> atomicComponentInstantiator;
    
    
    public CompositeComponentInstantiator(
            @Reference(name = "atomicComponentInstantiator") 
                ComponentInstantiator<Implementation<?>> atomicComponentInstantiator) {
        this.atomicComponentInstantiator = atomicComponentInstantiator;
    }

    @SuppressWarnings("unchecked")
    public LogicalComponent<CompositeImplementation> instantiate(LogicalComponent<CompositeImplementation> parent,
            ComponentDefinition<CompositeImplementation> definition, URI uri) throws InstantiationException {

        URI runtimeId = definition.getRuntimeId();
        LogicalComponent<CompositeImplementation> component = new LogicalComponent<CompositeImplementation>(uri, runtimeId, definition, parent);
        initializeProperties(component, definition);

        Composite composite = component.getDefinition().getImplementation().getComponentType();

        // create the child components
        for (ComponentDefinition<? extends Implementation<?>> child : composite.getComponents().values()) {
            
            URI childUri = URI.create(uri + "/" + child.getName());
            if (CompositeImplementation.IMPLEMENTATION_COMPOSITE.equals(child.getImplementation().getType())) {
                LogicalComponent<CompositeImplementation> childComponent = 
                    instantiate(component, (ComponentDefinition<CompositeImplementation>) child, childUri);
                component.addComponent(childComponent);
            } else {
                LogicalComponent<Implementation<?>> childComponent = 
                    atomicComponentInstantiator.instantiate(component, (ComponentDefinition<Implementation<?>>) child, childUri);
                component.addComponent(childComponent);
            }
            
        }
        
        instantiateCompositeServices(uri, component, composite);
        instantiateCompositeReferences(parent, uri, component, composite);

        return component;
        
    }

    private <I extends Implementation<?>> void instantiateCompositeServices(URI uri,
                                                                            LogicalComponent<I> component,
                                                                            Composite composite) {
        ComponentDefinition<I> definition = component.getDefinition();
        for (CompositeService service : composite.getServices().values()) {
            String name = service.getName();
            URI serviceUri = uri.resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            if (service.getPromote() != null) {
                logicalService.setPromote(URI.create(uri.toString() + "/" + service.getPromote()));
            }
            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
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

    private <I extends Implementation<?>> void instantiateCompositeReferences(
            LogicalComponent<CompositeImplementation> parent,
            URI uri,
            LogicalComponent<I> component,
            Composite composite) {
        
        ComponentDefinition<I> definition = component.getDefinition();
        
        // create logical references based on promoted references in the composite definition
        for (CompositeReference reference : composite.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = uri.resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);
            for (BindingDefinition binding : reference.getBindings()) {
                logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
            }
            for (URI promotedUri : reference.getPromoted()) {
                URI resolvedUri = URI.create(uri.toString() + "/" + promotedUri.toString());
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
                    logicalReference.overrideTargets(targets);
                }
            }
            component.addReference(logicalReference);
        }
    }

}
