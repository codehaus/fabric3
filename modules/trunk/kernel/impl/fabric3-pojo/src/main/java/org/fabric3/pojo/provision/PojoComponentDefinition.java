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
 * --- Original Apache License ---
 *
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
     * @param providerDefinition
     *         Instance factory provider definition.
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
