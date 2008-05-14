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
package org.fabric3.jpa.runtime;

import java.net.URI;

import javax.persistence.EntityManagerFactory;

import org.fabric3.jpa.provision.PersistenceUnitWireTargetDefinition;
import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.SingletonObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;

/**
 * Attaches the target side of entity manager factories.
 *
 * @version $Revision$ $Date$
 */
public class PersistenceUnitWireAttacher implements TargetWireAttacher<PersistenceUnitWireTargetDefinition> {
    
    private final EmfBuilder emfBuilder;
    private EmfClassLoaderService classLoaderService;

    /**
     * Injects the dependencies.
     *
     * @param emfBuilder         Entity manager factory builder.
     * @param classLoaderService the classloader service for returning EMF classloaders
     */
    public PersistenceUnitWireAttacher(@Reference EmfBuilder emfBuilder, @Reference EmfClassLoaderService classLoaderService) {
        this.emfBuilder = emfBuilder;
        this.classLoaderService = classLoaderService;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(PersistenceUnitWireTargetDefinition target) throws WiringException {
        
        final String unitName = target.getUnitName();
        URI classLoaderUri = target.getClassLoaderUri();
        final ClassLoader appCl = classLoaderService.getEmfClassLoader(classLoaderUri);
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();

        try {            
            Thread.currentThread().setContextClassLoader(appCl);
            EntityManagerFactory entityManagerFactory = emfBuilder.build(unitName, appCl);
            return new SingletonObjectFactory<EntityManagerFactory>(entityManagerFactory);
        } catch (EmfBuilderException e) {
            throw new WiringException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
        
    }

}
