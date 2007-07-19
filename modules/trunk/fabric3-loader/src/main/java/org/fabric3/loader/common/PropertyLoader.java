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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.w3c.dom.Document;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.model.type.Property;

/**
 * Loads a property declaration from an XML-based assembly file
 *
 * @version $Rev$ $Date$
 */
public class PropertyLoader implements StAXElementLoader<Property<?>> {
    private static final QName PROPERTY = new QName(SCA_NS, "Property");
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String ELEMENT = "element";
    private static final String MANY = "many";
    private static final String MUST_SUPPLY = "mustSupply";

    private final DocumentBuilder documentBuilder;

    public PropertyLoader() {
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            // we should be able to construct the default DocumentBuilder
            throw new AssertionError(e);
        }
    }

    public QName getXMLType() {
        return PROPERTY;
    }

    public Property<?> load(XMLStreamReader reader, LoaderContext ctx)
            throws XMLStreamException, LoaderException {
        assert PROPERTY.equals(reader.getName());
        String name = reader.getAttributeValue(null, NAME);
        String typeName = reader.getAttributeValue(null, TYPE);
        QName xmlType = null;
        if (typeName != null) {
            int index = typeName.indexOf(':');
            if (index != -1) {
                String prefix = typeName.substring(0, index);
                String localName = typeName.substring(index + 1);
                String ns = reader.getNamespaceURI(prefix);
                xmlType = new QName(ns, localName, prefix);
            }
        }
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, MANY));
        boolean mustSupply = Boolean.parseBoolean(reader.getAttributeValue(null, MUST_SUPPLY));
        Document value = PropertyUtils.createPropertyValue(reader, xmlType, documentBuilder);

        Property<?> property = new Property();
        property.setRequired(mustSupply);
        property.setName(name);
        property.setXmlType(xmlType);
        property.setMany(many);
        property.setDefaultValue(value);
        return property;
    }
}
