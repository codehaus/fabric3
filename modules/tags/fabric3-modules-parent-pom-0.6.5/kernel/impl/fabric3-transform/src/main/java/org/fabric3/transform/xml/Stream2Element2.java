/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
import org.fabric3.transform.TransformContext;
import org.fabric3.transform.AbstractPushTransformer;
import org.fabric3.transform.TransformationException;

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

    public void transform(XMLStreamReader reader, Element element, TransformContext context) throws TransformationException {

        try {
            Document document = element.getOwnerDocument();
            int depth = 0;
            while (true) {
                int next = reader.next();
                switch (next) {
                case START_ELEMENT:

                    Element child = document.createElementNS(reader.getNamespaceURI(), reader.getLocalName());

                    for (int i = 0; i < reader.getAttributeCount(); i++) {
                        child.setAttributeNS(reader.getAttributeNamespace(i),
                                             reader.getAttributeLocalName(i),
                                             reader.getAttributeValue(i));
                    }

                    // Handle namespaces
                    for (int i = 0; i < reader.getNamespaceCount(); i++) {

                        String prefix = reader.getNamespacePrefix(i);
                        String uri = reader.getNamespaceURI(i);

                        prefix = prefix == null ? "xmlns" : "xmlns:" + prefix;

                        child.setAttribute(prefix, uri);

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
        } catch (XMLStreamException e) {
            throw new TransformationException(e);
        }
    }
}
