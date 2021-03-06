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
 *
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
 */
package org.fabric3.introspection.xml;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

import org.fabric3.host.Namespaces;
import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidPrefixException;
import org.fabric3.spi.introspection.xml.InvalidQNamePrefix;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Default implementation of the loader helper.
 *
 * @version $Rev$ $Date$
 */
public class DefaultLoaderHelper implements LoaderHelper {
    private final DocumentBuilderFactory documentBuilderFactory;

    public DefaultLoaderHelper() {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public String loadKey(XMLStreamReader reader) {
        String key = reader.getAttributeValue(Namespaces.CORE, "key");
        if (key == null) {
            return null;
        }

        // TODO: we should copy all in-context namespaces to the declaration if we can find what they are
        // in the mean time, see if the value looks like it might contain a prefix
        int index = key.indexOf(':');
        if (index != -1 && !key.startsWith("{")) {
            // treat the key as a QName
            String prefix = key.substring(0, index);
            String localPart = key.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            key = "{" + ns + "}" + localPart;
        }
        return key;
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
        transform(reader, root);
        return value;
    }

    public void loadPolicySetsAndIntents(PolicyAware policyAware, XMLStreamReader reader, IntrospectionContext context) {
        try {
            policyAware.setIntents(parseListOfQNames(reader, "requires"));
            policyAware.setPolicySets(parseListOfQNames(reader, "policySets"));
        } catch (InvalidPrefixException e) {
            String prefix = e.getPrefix();
            URI uri = context.getContributionUri();
            context.addError(new InvalidQNamePrefix("The prefix " + prefix + " specified in contribution " + uri
                    + " is invalid", reader));
        }
    }


    public Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException {
        Set<QName> qNames = new HashSet<QName>();
        String val = reader.getAttributeValue(null, attribute);
        if (val != null) {
            StringTokenizer tok = new StringTokenizer(val);
            while (tok.hasMoreElements()) {
                qNames.add(createQName(tok.nextToken(), reader));
            }
        }
        return qNames;
    }

    public QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException {
        QName qName;
        int index = name.indexOf(':');
        if (index != -1) {
            String prefix = name.substring(0, index);
            String localPart = name.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                throw new InvalidPrefixException("Invalid prefix: " + prefix, prefix, reader);
            }
            qName = new QName(ns, localPart, prefix);
        } else {
            String prefix = "";
            String ns = reader.getNamespaceURI();
            qName = new QName(ns, name, prefix);
        }
        return qName;
    }

    public URI getURI(String target) {
        if (target == null) {
            return null;
        }

        int index = target.lastIndexOf('/');
        if (index == -1) {
            return URI.create(target);
        } else {
            String uri = target.substring(0, index);
            String fragment = target.substring(index + 1);
            return URI.create(uri + '#' + fragment);
        }
    }

    public List<URI> parseListOfUris(XMLStreamReader reader, String attribute) {
        String value = reader.getAttributeValue(null, attribute);
        if (value == null || value.length() == 0) {
            return null;
        } else {
            StringTokenizer tok = new StringTokenizer(value);
            List<URI> result = new ArrayList<URI>(tok.countTokens());
            while (tok.hasMoreTokens()) {
                result.add(getURI(tok.nextToken().trim()));
            }
            return result;
        }
    }

    private void transform(XMLStreamReader reader, Element element) throws XMLStreamException {
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
    }

}
