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
import javax.transaction.TransactionManager;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.provision.PersistenceContextWireTargetDefinition;
import org.fabric3.jpa.spi.classloading.EmfClassLoaderService;
import org.fabric3.jpa.spi.EmfBuilderException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the target side of entity manager factories.
 *
 * @version $Revision$ $Date$
 */
public class PersistenceContextWireAttacher implements TargetWireAttacher<PersistenceContextWireTargetDefinition> {
    private EmfBuilder emfBuilder;
    private EmfClassLoaderService classLoaderService;
    private TransactionManager tm;
    private EntityManagerService emService;

    /**
     * Constructor.
     *
     * @param emService          the service for creating EntityManagers
     * @param tm                 the transaction manager
     * @param emfBuilder         the EMF builder
     * @param classLoaderService the classloader service for returning EMF classloaders
     */
    public PersistenceContextWireAttacher(@Reference EntityManagerService emService,
                                          @Reference TransactionManager tm,
                                          @Reference EmfBuilder emfBuilder,
                                          @Reference EmfClassLoaderService classLoaderService) {
        this.emfBuilder = emfBuilder;
        this.classLoaderService = classLoaderService;
        this.emService = emService;
        this.tm = tm;
    }

    public ObjectFactory<?> createObjectFactory(PersistenceContextWireTargetDefinition definition) throws WiringException {
        String unitName = definition.getUnitName();
        boolean extended = definition.isExtended();
        URI classLoaderUri = definition.getClassLoaderId();
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            // get the classloader for the entity manager factory
            ClassLoader appCl = classLoaderService.getEmfClassLoader(classLoaderUri);
            Thread.currentThread().setContextClassLoader(appCl);
            // eagerly build the the EntityManagerFactory
            emfBuilder.build(unitName, appCl);
            if (definition.isMultiThreaded()) {
                return new MultiThreadedEntityManagerProxyFactory(unitName, extended, emService, tm);
            } else {
                return new StatefulEntityManagerProxyFactory(unitName, extended, emService, tm);
            }
        } catch (EmfBuilderException e) {
            throw new WiringException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, PersistenceContextWireTargetDefinition target, Wire wire) throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, PersistenceContextWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

}