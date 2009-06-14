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
package org.fabric3.pojo.provision;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * Definition of a physical component whose actual implementation is based on a POJO.
 *
 * @version $Rev$ $Date$
 */
public abstract class PojoComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = 297672484973345029L;

    private InstanceFactoryDefinition providerDefinition;
    private final Map<String, Document> propertyValues = new HashMap<String, Document>();

    /**
     * Gets the instance factory provider definition.
     *
     * @return Instance factory provider definition.
     */
    public InstanceFactoryDefinition getProviderDefinition() {
        return providerDefinition;
    }

    /**
     * Sets the instance factory provider definition.
     *
     * @param providerDefinition Instance factory provider definition.
     */
    public void setProviderDefinition(InstanceFactoryDefinition providerDefinition) {
        this.providerDefinition = providerDefinition;
    }

    /**
     * Return all property values.
     *
     * @return a Map containing all property values keyed by name
     */
    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    /**
     * Return the value of the property with the supplied name.
     *
     * @param name the name of the property
     * @return the property's value
     */
    public Document getPropertyValue(String name) {
        return propertyValues.get(name);
    }

    /**
     * Sets the value for a property.
     *
     * @param name  the name of the property
     * @param value its value
     */
    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }
}
