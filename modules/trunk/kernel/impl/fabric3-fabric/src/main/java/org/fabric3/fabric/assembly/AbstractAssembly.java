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

import java.util.Collection;

import javax.xml.namespace.QName;

import org.fabric3.fabric.assembly.allocator.AllocationException;
import org.fabric3.fabric.assembly.allocator.Allocator;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.logical.LogicalChange;
import org.fabric3.fabric.model.physical.PhysicalModelGenerator;
import org.fabric3.fabric.services.routing.RoutingException;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.assembly.ActivateException;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.assembly.AssemblyException;
import org.fabric3.spi.generator.CommandMap;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.runtime.assembly.LogicalComponentManager;
import org.fabric3.spi.runtime.assembly.RecordException;
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

    protected final PhysicalModelGenerator physicalModelGenerator;
    private final LogicalModelGenerator logicalModelGenerator;
    protected final Allocator allocator;
    private final MetaDataStore metadataStore;
    protected final LogicalComponentManager logicalComponentManager;

    public AbstractAssembly(Allocator allocator,
                            MetaDataStore metadataStore,
                            PhysicalModelGenerator physicalModelGenerator,
                            LogicalModelGenerator logicalModelGenerator,
                            LogicalComponentManager logicalComponentManager) {
        this.allocator = allocator; 
        this.metadataStore = metadataStore;
        this.physicalModelGenerator = physicalModelGenerator;
        this.logicalModelGenerator = logicalModelGenerator;
        this.logicalComponentManager = logicalComponentManager;
    }

    public void initialize() throws AssemblyException {
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
        
        LogicalCompositeComponent domain = logicalComponentManager.getDomain();
        
        LogicalChange change = logicalModelGenerator.include(domain, composite);
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
            throw new ActivateException(e);
        }

        try {
            // generate and provision any new components and new wires
            CommandMap commandMap = physicalModelGenerator.generate(components);
            physicalModelGenerator.provision(commandMap);
        } catch (GenerationException e) {
            throw new ActivateException(e);
        } catch (RoutingException e) {
            throw new ActivateException(e);
        }
        
        try {
            // record the operation
            logicalComponentManager.store();
        } catch (RecordException e) {
            throw new ActivateException("Error activating deployable", composite.getName().toString(), e);
        }
        
    }

}
