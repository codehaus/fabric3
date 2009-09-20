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

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamConstants;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.CHARACTERS;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.DTD;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.PROCESSING_INSTRUCTION;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;

import org.osoa.sca.annotations.EagerInit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import org.fabric3.model.type.service.DataType;
import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.AbstractPullTransformer;
import org.fabric3.spi.transform.TransformContext;

/**
 * Pull transformer that will convert a Stax stream to a DOM representation. The transformer expects the cursor to be at the element from which the
 * info set needs to transferred into the DOM tree.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class Stream2DocumentTransformer extends AbstractPullTransformer<XMLStreamReader, Document> {

    private static final JavaClass<Document> TARGET = new JavaClass<Document>(Document.class);
    private static final DocumentBuilderFactory FACTORY;

    static {
        FACTORY = DocumentBuilderFactory.newInstance();
        FACTORY.setNamespaceAware(true);
    }

    public DataType<?> getTargetType() {
        return TARGET;
    }

    public Document transform(XMLStreamReader reader, TransformContext context) throws TransformationException {

        if (reader.getEventType() != XMLStreamConstants.START_ELEMENT) {
            throw new TransformationException("The stream needs to be at te start of an element");
        }

        DocumentBuilder builder = getDocumentBuilder();
        Document document = builder.newDocument();

        QName rootName = reader.getName();
        Element root = createElement(reader, document, rootName);

        document.appendChild(root);

        try {
            while (true) {

                int next = reader.next();
                switch (next) {
                case START_ELEMENT:

                    QName childName = new QName(reader.getNamespaceURI(), reader.getLocalName());
                    Element child = createElement(reader, document, childName);

                    root.appendChild(child);
                    root = child;

                    break;

                case CHARACTERS:
                case CDATA:
                    Text text = document.createTextNode(reader.getText());
                    root.appendChild(text);
                    break;
                case END_ELEMENT:
                    if (rootName.equals(reader.getName())) {
                        return document;
                    }
                    root = (Element) root.getParentNode();
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

    private DocumentBuilder getDocumentBuilder() throws TransformationException {
        try {
            return FACTORY.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new TransformationException(e);
        }
    }

    /*
     * Creates the element and populates the namespace declarations and attributes.
     */
    private Element createElement(XMLStreamReader reader, Document document, QName rootName) {

        Element root = document.createElementNS(rootName.getNamespaceURI(), rootName.getLocalPart());

        // Handle namespace declarations
        for (int i = 0; i < reader.getNamespaceCount(); i++) {

            String prefix = reader.getNamespacePrefix(i);
            String uri = reader.getNamespaceURI(i);

            prefix = prefix == null ? "xmlns" : "xmlns:" + prefix;

            root.setAttribute(prefix, uri);

        }

        // Handle attributes
        for (int i = 0; i < reader.getAttributeCount(); i++) {

            String attributeNs = reader.getAttributeNamespace(i);
            String localName = reader.getAttributeLocalName(i);
            String value = reader.getAttributeValue(i);
            String attributePrefix = reader.getAttributePrefix(i);
            String qualifiedName = attributePrefix == null ? localName : attributePrefix + ":" + localName;

            root.setAttributeNS(attributeNs, qualifiedName, value);

        }

        return root;

    }

}
