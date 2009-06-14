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
package org.fabric3.fabric.builder.component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.builder.BuilderNotFoundException;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.ComponentBuilder;
import org.fabric3.spi.component.Component;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Default map-based implementation of the component builder registry.
 * <p/>
 *
 * @version $Rev$ $Date$
 */
public class DefaultComponentBuilderRegistry implements ComponentBuilderRegistry {

    // Internal cache
    private Map<Class<?>,
            ComponentBuilder<? extends PhysicalComponentDefinition, ? extends Component>> registry =
            new ConcurrentHashMap<Class<?>,
                    ComponentBuilder<? extends PhysicalComponentDefinition, ? extends Component>>();

    @Reference(required = false)
    public void setRegistry(Map<Class<?>,
            ComponentBuilder<? extends PhysicalComponentDefinition, ? extends Component>> registry) {
        this.registry = registry;
    }

    /**
     * Registers a physical component builder.
     *
     * @param <PCD>           Type of the physical component definition.
     * @param definitionClass Class of the physical component definition.
     * @param builder         Builder for the physical component definition.
     */
    public <PCD extends PhysicalComponentDefinition,
            C extends Component> void register(Class<?> definitionClass, ComponentBuilder<PCD, C> builder) {
        registry.put(definitionClass, builder);
    }

    /**
     * Builds a physical component from component definition.
     *
     * @param componentDefinition Component definition.
     * @return Component to be built.
     */
    @SuppressWarnings("unchecked")
    public Component build(PhysicalComponentDefinition componentDefinition) throws BuilderException {

        ComponentBuilder builder = registry.get(componentDefinition.getClass());
        if (builder == null) {
            throw new BuilderNotFoundException("Builder not found for " + componentDefinition.getClass().getName());
        }
        return builder.build(componentDefinition);

    }

}
