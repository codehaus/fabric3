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
    public static final String PROPERTY_NAME_ATTR = "name";
    public static final String PROPERTY_TYPE_ATTR = "type";
    public static final String PROPERTY_MANY_ATTR = "many";
    public static final String REQUIRED_ATTR = "override";

    private static final QName PROPERTY = new QName(SCA_NS, "Property");
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
        String name = reader.getAttributeValue(null, PROPERTY_NAME_ATTR);
        String typeName = reader.getAttributeValue(null, PROPERTY_TYPE_ATTR);
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
        boolean many = Boolean.parseBoolean(reader.getAttributeValue(null, PROPERTY_MANY_ATTR));
        String required = reader.getAttributeValue(null, REQUIRED_ATTR);
        Document value = PropertyUtils.createPropertyValue(reader, xmlType, documentBuilder);

        Property<?> property = new Property();
        property.setRequired(Boolean.parseBoolean(required));
        property.setName(name);
        property.setXmlType(xmlType);
        property.setMany(many);
        property.setDefaultValue(value);
        return property;
    }
}
