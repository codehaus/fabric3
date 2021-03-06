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
package org.fabric3.pojo.instancefactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.pojo.provision.InstanceFactoryDefinition;

/**
 * Default implementation of the registry.
 *
 * @version $Revison$ $Date$
 */
public class DefaultInstanceFactoryBuilderRegistry implements InstanceFactoryBuilderRegistry {

    // Internal cache
    private Map<Class<?>, InstanceFactoryBuilder<? extends InstanceFactoryProvider,
            ? extends InstanceFactoryDefinition>> registry =
            new ConcurrentHashMap<Class<?>, InstanceFactoryBuilder<? extends InstanceFactoryProvider,
                    ? extends InstanceFactoryDefinition>>();

    /**
     * Builds an instnace factory provider from a definition.
     *
     * @param providerDefinition Provider definition.
     * @param cl                 Clasloader to use.
     * @return Instance factory provider.
     */
    @SuppressWarnings("unchecked")
    public InstanceFactoryProvider build(InstanceFactoryDefinition providerDefinition, ClassLoader cl) throws InstanceFactoryBuilderException {
        Class<? extends InstanceFactoryDefinition> type = providerDefinition.getClass();
        InstanceFactoryBuilder builder = registry.get(type);
        if (builder == null) {
            String id = type.toString();
            throw new NoRegisteredIFBuilderException("No registered builder for: " + id, id);
        }
        return builder.build(providerDefinition, cl);
    }

    /**
     * Registers the builder.
     */
    public <IFPD extends InstanceFactoryDefinition> void register(Class<?> ifpdClass, InstanceFactoryBuilder<?, IFPD> builder) {
        registry.put(ifpdClass, builder);
    }

}
