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
package org.fabric3.fabric.domain;

import java.util.Collection;
import javax.xml.namespace.QName;

import static org.osoa.sca.Constants.SCA_NS;

import org.fabric3.fabric.allocator.AllocationException;
import org.fabric3.fabric.allocator.Allocator;
import org.fabric3.fabric.generator.PhysicalModelGenerator;
import org.fabric3.fabric.instantiator.LogicalChange;
import org.fabric3.fabric.instantiator.LogicalInstantiationException;
import org.fabric3.fabric.instantiator.LogicalModelInstantiator;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.fabric.services.routing.RoutingService;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.domain.DeploymentException;
import org.fabric3.spi.domain.Domain;
import org.fabric3.spi.domain.DomainException;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.contribution.MetaDataStoreException;
import org.fabric3.spi.services.contribution.QNameSymbol;
import org.fabric3.spi.services.contribution.ResourceElement;
import org.fabric3.spi.services.lcm.LogicalComponentManager;
import org.fabric3.spi.services.lcm.StoreException;

/**
 * Base class for abstract assemblies
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDomain implements Domain {
    public static final QName COMPOSITE = new QName(SCA_NS, "composite");

    private final PhysicalModelGenerator physicalModelGenerator;
    private final LogicalModelInstantiator logicalModelInstantiator;
    private final Allocator allocator;
    private final MetaDataStore metadataStore;
    private final LogicalComponentManager logicalComponentManager;
    private RoutingService routingService;

    public AbstractDomain(Allocator allocator,
                          MetaDataStore metadataStore,
                          PhysicalModelGenerator physicalModelGenerator,
                          LogicalModelInstantiator logicalModelInstantiator,
                          LogicalComponentManager logicalComponentManager,
                          RoutingService routingService) {
        this.allocator = allocator;
        this.metadataStore = metadataStore;
        this.physicalModelGenerator = physicalModelGenerator;
        this.logicalModelInstantiator = logicalModelInstantiator;
        this.logicalComponentManager = logicalComponentManager;
        this.routingService = routingService;
    }

    public void initialize() throws DomainException {
    }

    public void include(QName deployable) throws DeploymentException {

        ResourceElement<QNameSymbol, ?> element;
        try {
            element = metadataStore.resolve(new QNameSymbol(deployable));
        } catch (MetaDataStoreException e) {
            throw new DeploymentException("Error deploying: " + deployable, e);
        }
        if (element == null) {
            String id = deployable.toString();
            throw new DeployableNotFoundException("Deployable not found: " + id, id);
        }

        Object object = element.getValue();
        if (!(object instanceof Composite)) {
            String id = deployable.toString();
            throw new IllegalDeployableTypeException("Deployable must be a composite:" + id, id);
        }

        Composite composite = (Composite) object;
        include(composite);

    }

    public void include(Composite composite) throws DeploymentException {

        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        LogicalChange change;
        try {
            change = logicalModelInstantiator.include(domain, composite);
        } catch (LogicalInstantiationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }
        change.apply();

        Collection<LogicalComponent<?>> components = domain.getComponents();

        // Allocate the components to runtime nodes
        try {
            for (LogicalComponent<?> component : components) {
                if (!component.isProvisioned()) {
                    allocator.allocate(component, false);
                }
            }
        } catch (AllocationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }

        try {
            // generate and provision any new components and new wires
            CommandMap commandMap = physicalModelGenerator.generate(components);
            routingService.route(commandMap);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }

        try {
            // record the operation
            logicalComponentManager.store();
        } catch (StoreException e) {
            String id = composite.getName().toString();
            throw new DeploymentException("Error activating deployable: " + id, id, e);
        }

    }

    public void remove(QName deployable) throws DeploymentException {

        ResourceElement<QNameSymbol, ?> element;
        try {
            element = metadataStore.resolve(new QNameSymbol(deployable));
        } catch (MetaDataStoreException e) {
            throw new DeploymentException(e);
        }
        if (element == null) {
            String id = deployable.toString();
            throw new DeployableNotFoundException("Deployable not found " + id, id);
        }

        Object object = element.getValue();
        if (!(object instanceof Composite)) {
            String id = deployable.toString();
            throw new IllegalDeployableTypeException("Deployable must be a composite: " + id, id);
        }

        Composite composite = (Composite) object;
        remove(composite);

    }

    public void remove(Composite composite) throws DeploymentException {

        LogicalCompositeComponent domain = logicalComponentManager.getRootComponent();

        LogicalChange change;
        try {
            change = logicalModelInstantiator.remove(domain, composite);
        } catch (LogicalInstantiationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }
        change.apply();

        Collection<LogicalComponent<?>> components = change.getAddedComponents();

        // Allocate the components to runtime nodes
        try {
            for (LogicalComponent<?> component : components) {
                if (!component.isProvisioned()) {
                    allocator.allocate(component, false);
                }
            }
        } catch (AllocationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }

        try {
            // generate and provision any new components and new wires
            CommandMap commandMap = physicalModelGenerator.generate(change);
            routingService.route(commandMap);
        } catch (GenerationException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        } catch (RoutingException e) {
            throw new DeploymentException("Error deploying: " + composite.getName(), e);
        }

        try {
            // record the operation
            logicalComponentManager.store();
        } catch (StoreException e) {
            String id = composite.getName().toString();
            throw new DeploymentException("Error activating deployable: " + id, id, e);
        }

    }
}
