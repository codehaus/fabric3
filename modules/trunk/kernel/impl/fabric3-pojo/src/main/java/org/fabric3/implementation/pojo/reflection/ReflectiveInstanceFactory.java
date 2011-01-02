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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.implementation.pojo.reflection;

import org.fabric3.implementation.pojo.instancefactory.InstanceFactory;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.objectfactory.Injector;
import org.fabric3.spi.objectfactory.ObjectCreationException;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * @version $Rev$ $Date$
 */
public class ReflectiveInstanceFactory implements InstanceFactory {
    private final ObjectFactory<?> constructor;
    private Injectable[] injectables;
    private final Injector<Object>[] injectors;
    private final EventInvoker initInvoker;
    private final EventInvoker destroyInvoker;
    private final ClassLoader cl;
    private final boolean reinjectable;

    public ReflectiveInstanceFactory(ObjectFactory<?> constructor,
                                     Injectable[] injectables,
                                     Injector<Object>[] injectors,
                                     EventInvoker initInvoker,
                                     EventInvoker destroyInvoker,
                                     boolean reinjectable,
                                     ClassLoader cl) {
        this.constructor = constructor;
        this.injectables = injectables;
        this.injectors = injectors;
        this.initInvoker = initInvoker;
        this.destroyInvoker = destroyInvoker;
        this.reinjectable = reinjectable;
        this.cl = cl;
    }

    public InstanceWrapper newInstance(WorkContext workContext) throws ObjectCreationException {
        // push the work context onto the thread when calling the user object
        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(cl);
        WorkContext oldContext = WorkContextTunnel.setThreadWorkContext(workContext);
        try {
            Object instance = constructor.getInstance();
            if (injectors != null) {
                for (Injector<Object> injector : injectors) {
                    injector.inject(instance);
                }
            }
            return new ReflectiveInstanceWrapper(instance, reinjectable, cl, initInvoker, destroyInvoker, injectables, injectors);
        } finally {
            WorkContextTunnel.setThreadWorkContext(oldContext);
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }
}
