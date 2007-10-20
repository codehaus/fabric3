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
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;

import org.fabric3.scdl.Property;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * Loads a property declaration from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class PropertyLoader implements StAXElementLoader<Property<?>> {
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ELEMENT = "element";
    private static final String MANY = "many";
    private static final String MUST_SUPPLY = "mustSupply";
    private static final QName XS_STRING = new QName(XMLConstants.W3C_XML_SCHEMA_NS_URI, "string");

    private final DocumentBuilder documentBuilder;

    public PropertyLoader() {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // we should be able to construct the default DocumentBuilder
            throw new AssertionError(e);
        }
    }

    public Property<?> load(XMLStreamReader reader, LoaderContext ctx)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, NAME);
        String typeName = reader.getAttributeValue(null, TYPE);
        String elementName = reader.getAttributeValue(null, ELEMENT);
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, MANY));
        boolean mustSupply = Boolean.parseBoolean(reader.getAttributeValue(null, MUST_SUPPLY));

        NamespaceContext namespaceContext = reader.getNamespaceContext();
        QName xmlType;
        Document value;
        if (elementName != null) {
            if (typeName != null) {
                throw new InvalidValueException("Cannot specify both type and element", name);
            }
            QName element = LoaderUtil.getQName(elementName, null, namespaceContext);
            throw new UnsupportedOperationException();
        } else {
            if (typeName == null) {
                xmlType = LoaderUtil.getQName(elementName, null, namespaceContext);
            } else {
                xmlType = XS_STRING;
            }
            value = PropertyUtils.createPropertyValue(reader, xmlType, documentBuilder);
        }

        Property<?> property = new Property();
        property.setRequired(mustSupply);
        property.setName(name);
        property.setXmlType(xmlType);
        property.setMany(many);
        property.setDefaultValue(value);
        return property;
    }
}
