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
package org.fabric3.scdl;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * A component property as declared in the component type.
 *
 * @version $Rev$ $Date$
 */
public class Property extends ModelObject {
    private String name;
    private boolean many;
    private boolean required;
    private QName xmlType;

    private Document defaultValue;

    public Property() {
    }

    public Property(String name, QName xmlType) {
        this.name = name;
        this.xmlType = xmlType;
    }

    /**
     * Returns the name of the property.
     *
     * @return the name of the property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the property.
     *
     * @param name the name of the property
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns whether the property is many-valued or single-valued.
     *
     * @return true if the property is many-valued
     */
    public boolean isMany() {
        return many;
    }

    /**
     * Sets whether the property is many-valued or single-valued.
     *
     * @param many true if the property is many-valued
     */
    public void setMany(boolean many) {
        this.many = many;
    }

    /**
     * Returns whether the component definition must supply a value for this property.
     *
     * @return true if the component definition must supply a value
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * Sets whether the component definition must supply a value for this property.
     *
     * @param required true if the component definition must supply a value
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    public QName getXmlType() {
        return xmlType;
    }

    public void setXmlType(QName xmlType) {
        this.xmlType = xmlType;
    }

    public Document getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Document defaultValue) {
        this.defaultValue = defaultValue;
    }
}
