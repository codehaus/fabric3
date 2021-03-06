/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.scdl;

import java.net.URI;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

/**
 * Represents the value of a configured component property. The value can be specified: <ul> <li>as an XML value in the
 * component definition</li> <li>as a reference to an external resource<li> <li>as the result of an XPath
 * expression</li> <ul>
 *
 * @version $Rev$ $Date$
 */
public class PropertyValue extends ModelObject {
    private static final long serialVersionUID = -1638553201072873854L;
    private String name;
    private String source;
    private URI file;
    private DataType<QName> valueType;
    private Document value;

    public PropertyValue() {
    }

    /**
     * Constructor specifying the name of a property and the XPath source expression.
     *
     * @param name   the name of the property which this value is for
     * @param source an XPath expression whose result will be the actual value
     */
    public PropertyValue(String name, String source) {
        this.name = name;
        this.source = source;
    }

    /**
     * Constructor specifying the name of a property loaded from an exteral resource.
     *
     * @param name the name of the property which this value is for
     * @param file A URI that the property value can be loaded from
     */
    public PropertyValue(String name, URI file) {
        this.name = name;
        this.file = file;
    }

    /**
     * @param name      the name of the property
     * @param valueType the XML type of the value
     * @param value     the property value
     */
    public PropertyValue(String name, DataType<QName> valueType, Document value) {
        this.name = name;
        this.valueType = valueType;
        this.value = value;
    }

    /**
     * Returns the name of the property that this value is for.
     *
     * @return the name of the property that this value is for
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the property that this value is for.
     *
     * @param name the name of the property that this value is for
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns an XPath expression that should be evaluated to get the actual property value.
     *
     * @return an XPath expression that should be evaluated to get the actual property value
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets an XPath expression that should be evaluated to get the actual property value.
     *
     * @param source an XPath expression that should be evaluated to get the actual property value
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Returns the location of the resource containing the property value.
     *
     * @return the location of the resource containing the property value
     */
    public URI getFile() {
        return file;
    }

    /**
     * Sets the location of the resource containing the property value
     *
     * @param file the location of the resource containing the property value
     */
    public void setFile(URI file) {
        this.file = file;
    }

    /**
     * Returns the XML value of the property.
     *
     * @return the XML value of the property
     */
    public Document getValue() {
        return value;
    }

    /**
     * Sets the XML value of the property.
     *
     * @param value the XML value of the property
     */
    public void setValue(Document value) {
        this.value = value;
    }

    /**
     * Returns the value's XML Schema type.
     *
     * @return the value's XML Schema type
     */
    public DataType<QName> getValueType() {
        return valueType;
    }

    /**
     * Sets the value's XML Schema type.
     *
     * @param valueType the value's XML Schema type
     */
    public void setValueType(DataType<QName> valueType) {
        this.valueType = valueType;
    }
}
