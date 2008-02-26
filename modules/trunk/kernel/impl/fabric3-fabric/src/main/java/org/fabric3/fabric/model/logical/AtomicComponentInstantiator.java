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

/**
 * @version $Revision$ $Date$
 */
public class AtomicComponentInstantiator extends AbstractComponentInstantiator {

    public AtomicComponentInstantiator(@Reference(name = "documentLoader")DocumentLoader documentLoader) {
        super(documentLoader);
    }

    public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalCompositeComponent parent, ComponentDefinition<I> definition)
            throws InstantiationException {

        URI runtimeId = definition.getRuntimeId();
        URI uri = URI.create(parent.getUri() + "/" + definition.getName());
        LogicalComponent<I> component = new LogicalComponent<I>(uri, runtimeId, definition, parent);

        initializeProperties(component, definition);

        I impl = definition.getImplementation();
        AbstractComponentType<?, ?, ?, ?> componentType = impl.getComponentType();

        createServices(definition, component, componentType);
        createReferences(definition, component, componentType);
        createResources(component, componentType);

        return component;

    }

    private <I extends Implementation<?>> void createResources(LogicalComponent<I> component, AbstractComponentType<?, ?, ?, ?> componentType) {

        for (ResourceDefinition resource : componentType.getResources().values()) {
            URI resourceUri = component.getUri().resolve('#' + resource.getName());
            LogicalResource<?> logicalResource = createLogicalResource(resource, resourceUri, component);
            component.addResource(logicalResource);
        }

    }

    private <I extends Implementation<?>> void createServices(ComponentDefinition<I> definition,
                                                              LogicalComponent<I> component,
                                                              AbstractComponentType<?, ?, ?, ?> componentType) {

        for (ServiceDefinition service : componentType.getServices().values()) {
            String name = service.getName();
            URI serviceUri = component.getUri().resolve('#' + name);
            LogicalService logicalService = new LogicalService(serviceUri, service, component);
            ComponentService componentService = definition.getServices().get(name);
            if (componentService != null) {
                addOperationLevelIntentsAndPolicies(logicalService, componentService);
                // service is configured in the component definition
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
            ComponentReference componentReference = definition.getReferences().get(name);
            if (componentReference != null) {
                addOperationLevelIntentsAndPolicies(logicalReference, componentReference);
                // reference is configured
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

    /*
     * Creates a logical resource.
     */
    private <RD extends ResourceDefinition> LogicalResource<RD> createLogicalResource(RD resourceDefinition,
                                                                                      URI resourceUri,
                                                                                      LogicalComponent<?> component) {
        return new LogicalResource<RD>(resourceUri, resourceDefinition, component);
    }

}
