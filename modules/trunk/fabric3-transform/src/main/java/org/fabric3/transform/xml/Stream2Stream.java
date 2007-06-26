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
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.fabric3.spi.model.type.DataType;
import org.fabric3.spi.transform.TransformContext;
import org.fabric3.transform.AbstractPushTransformer;

/**
 * @version $Rev$ $Date$
 */
public class Stream2Stream extends AbstractPushTransformer<XMLStreamReader, XMLStreamWriter> {
    public DataType<?> getSourceType() {
        return null;
    }

    public DataType<?> getTargetType() {
        return null;
    }

    public void transform(XMLStreamReader reader, XMLStreamWriter writer, TransformContext context) throws XMLStreamException {
        int level = 0;
        do {
            switch (reader.next()) {
            case START_DOCUMENT:
                writer.writeStartDocument(reader.getCharacterEncodingScheme(), reader.getVersion());
                level += 1;
                break;
            case START_ELEMENT:
                writer.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());
                for (int i = 0; i < reader.getAttributeCount(); i++) {
                    String prefix = reader.getAttributePrefix(i);
                    String namespaceUri = reader.getAttributeNamespace(i);
                    String localName = reader.getAttributeLocalName(i);
                    String value = reader.getAttributeValue(i);
                    writer.writeAttribute(prefix, namespaceUri, localName, value);
                }
                level += 1;
                break;
            case CHARACTERS:
            case CDATA:
                writer.writeCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
                break;
            case ENTITY_REFERENCE:
                writer.writeEntityRef(reader.getText());
                break;
            case END_ELEMENT:
                writer.writeEndElement();
                level -= 1;
                break;
            case END_DOCUMENT:
                writer.writeEndDocument();
                return;
            case COMMENT:
            case SPACE:
            case PROCESSING_INSTRUCTION:
            case DTD:
                // ignore these for now
                break;
            }
        } while (level != 0);
    }
}
