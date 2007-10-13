/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.loader.composite;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.loader.common.InvalidNameException;
import org.fabric3.spi.loader.InvalidValueException;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.StAXElementLoader;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.model.type.XSDSimpleType;
import org.fabric3.transform.xml.Stream2Element2;

/**
 * @version $Rev$ $Date$
 */
public class PropertyValueLoader implements StAXElementLoader<PropertyValue> {
    private static final QName PROPERTY = new QName(SCA_NS, "property");

    private final Stream2Element2 stream2Element;
    private final DocumentBuilderFactory documentBuilderFactory;

    public PropertyValueLoader() {
        // TODO get the transformers by injection
        stream2Element = new Stream2Element2(PROPERTY);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public PropertyValue load(XMLStreamReader reader, LoaderContext context)
            throws XMLStreamException, LoaderException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            throw new InvalidNameException(name);
        }

        String source = reader.getAttributeValue(null, "source");
        String file = reader.getAttributeValue(null, "file");
        if (source != null) {
            LoaderUtil.skipToEndElement(reader);
            return new PropertyValue(name, source);
        } else if (file != null) {
            try {
                URI uri = new URI(file);
                if (!uri.isAbsolute()) {
                    uri = context.getSourceBase().toURI().resolve(uri);
                }
                LoaderUtil.skipToEndElement(reader);
                return new PropertyValue(name, uri);
            } catch (URISyntaxException e) {
                throw new InvalidValueException(file, name, e);
            }
        } else {
            return loadInlinePropertyValue(name, reader);
        }
    }

    protected PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader)
            throws InvalidValueException, XMLStreamException {
        DataType<QName> dataType;
        String type = reader.getAttributeValue(null, "type");
        String element = reader.getAttributeValue(null, "element");
        if (type != null) {
            if (element != null) {
                throw new InvalidValueException("Cannot supply both type and element for property ", name);
            }
            // TODO support type attribute
            throw new UnsupportedOperationException();
        } else if (element != null) {
            // TODO support element attribute
            throw new UnsupportedOperationException();
        } else {
            dataType = new XSDSimpleType(Element.class, XSDSimpleType.STRING);
        }

        DocumentBuilder docBuilder;
        try {
            docBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError();
        }
        Document value = docBuilder.newDocument();
        Element valueElement = value.createElement("value");
        value.appendChild(valueElement);
        stream2Element.transform(reader, valueElement, null);
        return new PropertyValue(name, dataType, value);
    }
}
