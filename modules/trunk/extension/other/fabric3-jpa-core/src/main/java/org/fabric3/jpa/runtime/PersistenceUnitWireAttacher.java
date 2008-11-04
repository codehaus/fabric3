/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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

    public void detachFromTarget(PhysicalWireSourceDefinition source, PersistenceUnitWireTargetDefinition target) throws WiringException {
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
