/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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

import org.fabric3.scdl.DataType;
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.AbstractPushTransformer;
import org.fabric3.transform.TransformationException;

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

    public void transform(XMLStreamReader reader, XMLStreamWriter writer, TransformContext context) throws TransformationException {
        try {
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
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }
}
