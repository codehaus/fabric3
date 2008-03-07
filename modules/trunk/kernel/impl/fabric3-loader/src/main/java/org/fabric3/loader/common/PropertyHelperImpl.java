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
package org.fabric3.loader.common;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.fabric3.transform.xml.Stream2Element2;

/**
 * Default implementation of LoaderHelper.
 *
 * @version $Rev$ $Date$
 */
public class PropertyHelperImpl implements PropertyHelper {
    private final Stream2Element2 stream2Element;
    private final DocumentBuilderFactory documentBuilderFactory;

    public PropertyHelperImpl() {
        stream2Element = new Stream2Element2();
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public Document loadValue(XMLStreamReader reader) throws XMLStreamException {
        DocumentBuilder builder;
        try {
            builder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new AssertionError(e);
        }
        Document value = builder.newDocument();
        Element root = value.createElement("value");
        value.appendChild(root);
        stream2Element.transform(reader, root, null);
        return value;
    }
}
