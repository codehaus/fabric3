/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
package org.fabric3.implementation.pojo.instancefactory;

import java.lang.reflect.Type;

import org.fabric3.spi.model.type.java.Injectable;
import org.fabric3.spi.objectfactory.ObjectFactory;

/**
 * Creates configured instances of a component implementation.
 *
 * @version $Rev$ $Date$
 */
public interface InstanceFactoryProvider {

    /**
     * Return the implementation class.
     *
     * @return the implementation class.
     */
    Class<?> getImplementationClass();

    /**
     * Used to signal the start of a component configuration update.
     */
    void startUpdate();

    /**
     * Used to signal when a component configuration update is complete.
     */
    void endUpdate();

    /**
     * Sets an object factory for an injection site.
     *
     * @param name          the injection site name
     * @param objectFactory the object factory
     */
    void setObjectFactory(Injectable name, ObjectFactory<?> objectFactory);

    /**
     * Sets an object factory for an injection site that is associated with a key.
     *
     * @param name          the injection site
     * @param objectFactory the object factory
     * @param key           the key for Map-based injection sites
     */
    void setObjectFactory(Injectable name, ObjectFactory<?> objectFactory, Object key);

    /**
     * Returns a previously added object factory for the injection site.
     *
     * @param attribute the injection site
     * @return the object factory or null
     */
    ObjectFactory<?> getObjectFactory(Injectable attribute);

    /**
     * Removes an object factory for an injection site.
     *
     * @param name the injection site name
     */
    void removeObjectFactory(Injectable name);

    /**
     * Returns the type for the injection site
     *
     * @param attribute the injection site
     * @return the required type
     */
    Class<?> getMemberType(Injectable attribute);

    /**
     * Returns the generic type for the injection site
     *
     * @param attribute the injection site
     * @return the required type
     */
    Type getGenericType(Injectable attribute);

    /**
     * Create an instance factory that can be used to create component instances.
     *
     * @return a new instance factory
     */
    InstanceFactory createFactory();
}
