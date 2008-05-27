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
import java.util.Map;

import org.fabric3.fabric.assembly.InstantiationException;
import org.fabric3.fabric.services.documentloader.DocumentLoader;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentReference;
import org.fabric3.scdl.ComponentService;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ResourceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalResource;
import org.fabric3.spi.model.instance.LogicalService;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

/**
 * @version $Revision$ $Date$
 */
public class AtomicComponentInstantiator extends AbstractComponentInstantiator {

    public AtomicComponentInstantiator(@Reference(name = "documentLoader")DocumentLoader documentLoader) {
        super(documentLoader);
    }

    public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent,
                                                                         Map<String, Document> properties,
                                                                         ComponentDefinition<I> definition)
            throws InstantiationException {

        I impl = definition.getImplementation();
        AbstractComponentType<?, ?, ?, ?> componentType = impl.getComponentType();

        URI runtimeId = definition.getRuntimeId();
        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        LogicalComponent<I> component = new LogicalComponent<I>(uri, runtimeId, definition, parent);
        initializeProperties(component, definition);
        createServices(definition, component, componentType);
        createReferences(definition, component, componentType);
        createResources(component, componentType);
        return component;

    }

    private <I extends Implementation<?>> void createServices(ComponentDefinition<I> definition,
                                                              LogicalComponent<I> component,
                                                              AbstractComponentType<?, ?, ?, ?> componentType) {

        for (ServiceDefinition service : componentType.getServices().values()) {
            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);

            for (BindingDefinition binding : service.getBindings()) {
                logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }

            for (BindingDefinition binding : service.getCallbackBindings()) {
                logicalService.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
            }

            // service is configured in the component definition
            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                logicalService.addIntents(componentService.getIntents());
                addOperationLevelIntentsAndPolicies(logicalService, componentService);
                for (BindingDefinition binding : componentService.getBindings()) {
                    logicalService.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
                }
                for (BindingDefinition binding : componentService.getCallbackBindings()) {
                    logicalService.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalService));
                }
            }
            component.addService(logicalService);
        }

    }

    private <I extends Implementation<?>> void createReferences(ComponentDefinition<I> definition,
                                                                LogicalComponent<I> component,
                                                                AbstractComponentType<?, ?, ?, ?> componentType) {

        for (ReferenceDefinition reference : componentType.getReferences().values()) {
            String name = reference.getName();
            URI referenceUri = component.getUri().resolve('#' + name);
            LogicalReference logicalReference = new LogicalReference(referenceUri, reference, component);

            // reference is configured in the component definition
            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                logicalReference.addIntents(componentReference.getIntents());
                addOperationLevelIntentsAndPolicies(logicalReference, componentReference);
                for (BindingDefinition binding : componentReference.getBindings()) {
                    logicalReference.addBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
                }
                for (BindingDefinition binding : componentReference.getCallbackBindings()) {
                    logicalReference.addCallbackBinding(new LogicalBinding<BindingDefinition>(binding, logicalReference));
                }
            }
            component.addReference(logicalReference);
        }

    }

    private void createResources(LogicalComponent<?> component, AbstractComponentType<?, ?, ?, ?> componentType) {

        for (ResourceDefinition resource : componentType.getResources().values()) {
            URI resourceUri = component.getUri().resolve('#' + resource.getName());
            LogicalResource<ResourceDefinition> logicalResource = new LogicalResource<ResourceDefinition>(resourceUri, resource, component);
            component.addResource(logicalResource);
        }

    }

}
