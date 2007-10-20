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
package org.fabric3.transform.xml;

import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.fabric3.scdl.DataType;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPushTransformer;

/**
 * @version $Rev$ $Date$
 */
public class Stream2Element2 extends AbstractPushTransformer<XMLStreamReader, Element> {

    public Stream2Element2() {
    }

    public DataType<?> getSourceType() {
        return null;
    }

    public DataType<?> getTargetType() {
        return null;
    }

    public void transform(XMLStreamReader reader, Element element, TransformContext context) throws XMLStreamException {

        Document document = element.getOwnerDocument();
        int depth = 0;
        while (true) {
            int next = reader.next();
            switch (next) {
            case START_ELEMENT:
                Element child = document.createElementNS(reader.getNamespaceURI(), reader.getLocalName());
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    child.setAttributeNS(reader.getNamespaceURI(i),
                                         reader.getAttributeLocalName(i),
                                         reader.getAttributeValue(i));
                }
                element.appendChild(child);
                element = child;
                depth++;
                break;
            case CHARACTERS:
            case CDATA:
                Text text = document.createTextNode(reader.getText());
                element.appendChild(text);
                break;
            case END_ELEMENT:
                if (depth == 0) {
                    return;
                }
                depth--;
                element = (Element) element.getParentNode();
            case ENTITY_REFERENCE:
            case COMMENT:
            case SPACE:
            case PROCESSING_INSTRUCTION:
            case DTD:
                break;
            }
        }
    }
}
