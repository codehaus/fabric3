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
package org.fabric3.loader.common;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.Property;

/**
 * Loads a property declaration from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class PropertyLoader implements TypeLoader<Property> {
    private static final String NAME = "name";
    private static final String MANY = "many";
    private static final String MUST_SUPPLY = "mustSupply";
    private static final QName XS_STRING = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string");

    private final LoaderHelper helper;

    public PropertyLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public Property load(XMLStreamReader reader, IntrospectionContext ctx) throws XMLStreamException {
        String name = reader.getAttributeValue(null, NAME);
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, MANY));
        boolean mustSupply = Boolean.parseBoolean(reader.getAttributeValue(null, MUST_SUPPLY));
        Document value = helper.loadValue(reader);

        Property property = new Property();
        property.setRequired(mustSupply);
        property.setName(name);
        property.setXmlType(XS_STRING);
        property.setMany(many);
        property.setDefaultValue(value);
        return property;
    }
}
