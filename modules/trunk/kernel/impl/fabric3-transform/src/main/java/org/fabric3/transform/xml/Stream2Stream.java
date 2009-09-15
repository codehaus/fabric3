  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
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

  import org.fabric3.model.type.service.DataType;
  import org.fabric3.spi.transform.PushTransformer;
  import org.fabric3.spi.transform.TransformContext;
  import org.fabric3.spi.transform.TransformationException;

  /**
 * @version $Rev$ $Date$
 */
public class Stream2Stream implements PushTransformer<XMLStreamReader, XMLStreamWriter> {
      public boolean canTransform(DataType<?> type) {
          return false;
      }

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
