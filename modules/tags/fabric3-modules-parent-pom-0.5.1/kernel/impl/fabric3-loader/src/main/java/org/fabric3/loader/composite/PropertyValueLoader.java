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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.scdl.DataType;
import org.fabric3.scdl.PropertyValue;
import org.fabric3.spi.model.type.XSDSimpleType;

/**
 * @version $Rev$ $Date$
 */
public class PropertyValueLoader implements TypeLoader<PropertyValue> {
    private final LoaderHelper helper;

    public PropertyValueLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PropertyValue load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        String name = reader.getAttributeValue(null, "name");
        if (name == null || name.length() == 0) {
            MissingAttribute failure = new MissingAttribute("Missing name attribute", "name", reader);
            context.addError(failure);
            return null;
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
                InvalidValue failure = new InvalidValue("File specified for property " + name + " is invalid: " + file, name, reader, e);
                context.addError(failure);
                return null;
            }
        } else {
            return loadInlinePropertyValue(name, reader, context);
        }
    }

    private PropertyValue loadInlinePropertyValue(String name, XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        DataType<QName> dataType;
        String type = reader.getAttributeValue(null, "type");
        String element = reader.getAttributeValue(null, "element");
        if (type != null) {
            if (element != null) {
                InvalidValue failure = new InvalidValue("Cannot supply both type and element for property: " + name, name, reader);
                context.addError(failure);
                return null;
            }
            // TODO support type attribute
            throw new UnsupportedOperationException();
        } else if (element != null) {
            // TODO support element attribute
            throw new UnsupportedOperationException();
        } else {
            dataType = new XSDSimpleType(Element.class, XSDSimpleType.STRING);
        }

        Document value = helper.loadValue(reader);
        return new PropertyValue(name, dataType, value);
    }
}
