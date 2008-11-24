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
package org.fabric3.loader.definitions;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.fabric3.scdl.definitions.PolicyPhase;
import org.fabric3.scdl.definitions.PolicySet;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;
import org.fabric3.spi.transform.TransformationException;

/**
 * Loader for definitions.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class PolicySetLoader implements TypeLoader<PolicySet> {

    private static final DocumentBuilderFactory FACTORY;

    static {
        FACTORY = DocumentBuilderFactory.newInstance();
        FACTORY.setNamespaceAware(true);
    }

    private final LoaderHelper helper;

    public PolicySetLoader(@Reference LoaderHelper helper) {
        this.helper = helper;
    }

    public PolicySet load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);
        Element policyElement;
        try {
            policyElement = transform(reader).getDocumentElement();
        } catch (TransformationException e) {
            DefinitionProcessingFailure failure = new DefinitionProcessingFailure("Error processing policy set", e, reader);
            context.addError(failure);
            return null;
        }

        String name = policyElement.getAttribute("name");
        QName qName = new QName(context.getTargetNamespace(), name);

        Set<QName> provides = new HashSet<QName>();
        StringTokenizer tok = new StringTokenizer(policyElement.getAttribute("provides"));
        while (tok.hasMoreElements()) {
            try {
                provides.add(helper.createQName(tok.nextToken(), reader));
            } catch (InvalidPrefixException e) {
                context.addError(new InvalidQNamePrefix(e.getPrefix(), reader));
                return null;
            }
        }

        String appliesTo = policyElement.getAttribute("appliesTo");

        String sPhase = policyElement.getAttributeNS(Namespaces.POLICY, "phase");
        PolicyPhase phase = null;
        if (sPhase != null && !"".equals(sPhase.trim())) {
            phase = PolicyPhase.valueOf(sPhase);
        } else {
            phase = PolicyPhase.PROVIDED;
        }

        Element extension = null;
        NodeList children = policyElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element) {
                extension = (Element) children.item(i);
                break;
            }
        }

        return new PolicySet(qName, provides, appliesTo, extension, phase);

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"name".equals(name) && !"provides".equals(name) && !"appliesTo".equals(name) && !"phase".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }

    private Document transform(XMLStreamReader reader) throws TransformationException {

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
    
