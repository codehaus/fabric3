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

import static org.osoa.sca.Constants.SCA_NS;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.fabric3.fabric.assembly.allocator.AllocationException;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.assembly.RecordException;
import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalModelGenerator;
import org.fabric3.fabric.model.physical.PhysicalWireGenerator;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.assembly.BindException;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;

/**
 * Base class for abstract assemblies
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractAssembly implements Assembly {

    public static final QName COMPOSITE = new QName(SCA_NS, "composite");

    private final PhysicalModelGenerator physicalModelGenerator;
    private final LogicalModelGenerator logicalModelGenerator;
    private final Allocator allocator;
    private final RoutingService routingService;
    private final MetaDataStore metadataStore;
    private final LogicalComponentManager logicalComponentManager;
    private final PhysicalWireGenerator wireGenerator;

    public AbstractAssembly(Allocator allocator,
                            RoutingService routingService,
                            MetaDataStore metadataStore,
                            PhysicalModelGenerator physicalModelGenerator,
                            LogicalModelGenerator logicalModelGenerator,
                            LogicalComponentManager logicalComponentManager,
                            PhysicalWireGenerator wireGenerator) {
        this.allocator = allocator;
        this.routingService = routingService; 
        this.metadataStore = metadataStore;
        this.physicalModelGenerator = physicalModelGenerator;
        this.logicalModelGenerator = logicalModelGenerator;
        this.logicalComponentManager = logicalComponentManager;
        this.wireGenerator = wireGenerator;
    }

    public void initialize() throws AssemblyException {

        logicalComponentManager.initialize();
        Collection<LogicalComponent<?>> components = logicalComponentManager.getComponents();
        
        try {
            for (LogicalComponent<?> component : components) {
                allocator.allocate(component, false);
            }
        } catch (AllocationException e) {
            throw new ActivateException(e);
        }

        // generate and provision components on nodes that have gone down
        Map<URI, GeneratorContext> contexts = physicalModelGenerator.generate(components);
        physicalModelGenerator.provision(contexts);
        // TODO end temporary recovery code
        
    }

    public void includeInDomain(QName deployable) throws ActivateException {
        
        ResourceElement<QNameSymbol, ?> element = metadataStore.resolve(new QNameSymbol(deployable));
        if (element == null) {
            throw new ArtifactNotFoundException("Deployable not found", deployable.toString());
        }
        
        Object object = element.getValue();
        if (!(object instanceof Composite)) {
            throw new IllegalContributionTypeException("Deployable must be a composite", deployable.toString());
        }
        
        Composite composite = (Composite) object;
        includeInDomain(composite);
        
    }

    public void includeInDomain(Composite composite) throws ActivateException {
        
        LogicalComponent<CompositeImplementation> domain = logicalComponentManager.getDomain();
        List<LogicalComponent<?>> components = logicalModelGenerator.include(domain, composite);

        // Allocate the components to runtime nodes
        try {
            for (LogicalComponent<?> component : components) {
                allocator.allocate(component, false);
            }
        } catch (AllocationException e) {
            throw new ActivateException(e);
        }

        // generate and provision the new components
        Map<URI, GeneratorContext> contexts = physicalModelGenerator.generate(components);
        physicalModelGenerator.provision(contexts);
        
        try {
            // record the operation
            logicalComponentManager.store();
        } catch (RecordException e) {
            throw new ActivateException("Error activating deployable", composite.getName().toString(), e);
        }
        
    }


    public void bindService(URI serviceUri, BindingDefinition bindingDefinition) throws BindException {
        
        LogicalComponent<?> currentComponent = logicalComponentManager.getComponent(serviceUri);
        if (currentComponent == null) {
            throw new BindException("Component not found", serviceUri.toString());
        }
        
        String fragment = serviceUri.getFragment();
        LogicalService service;
        if (fragment == null) {
            if (currentComponent.getServices().size() != 1) {
                String uri = serviceUri.toString();
                throw new BindException("Component must implement one service if no service name specified", uri);
            }
            Collection<LogicalService> services = currentComponent.getServices();
            service = services.iterator().next();
        } else {
            service = currentComponent.getService(fragment);
            if (service == null) {
                throw new BindException("Service not found", serviceUri.toString());
            }
        }
        
        LogicalBinding<?> binding = new LogicalBinding<BindingDefinition>(bindingDefinition, service);
        PhysicalChangeSet changeSet = new PhysicalChangeSet();
        CommandSet commandSet = new CommandSet();
        GeneratorContext context = new DefaultGeneratorContext(changeSet, commandSet);
        
        try {
            wireGenerator.generateBoundServiceWire(service, binding, currentComponent, context);
            routingService.route(currentComponent.getRuntimeId(), changeSet);
            service.addBinding(binding);
            // TODO record to recovery service
        } catch (GenerationException e) {
            throw new BindException("Error binding service", serviceUri.toString(), e);
        } catch (RoutingException e) {
            throw new BindException(e);
        }
        
    }
    
    public <I extends Implementation<?>> LogicalComponent<I> instantiate(LogicalComponent<CompositeImplementation> parent,
            ComponentDefinition<I> definition) throws ActivateException {
        return logicalModelGenerator.instantiate(parent, definition);
    }

}
