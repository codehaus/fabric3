/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.spi.util.stax;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Utility for stax operations.
 *
 * @version $Revision$ $Date$
 */
public abstract class StaxUtil {

    private StaxUtil() {
    }

    /**
     * Parses a list of qualified names.
     *
     * @param reader    XML stream reader.
     * @param attribute Attribute that contains the list of qualified names.
     * @return Set containing the qualified names.
     * @throws InvalidPrefixException If the qualified name cannot be resolved.
     */
    public static Set<QName> parseListOfQNames(XMLStreamReader reader, String attribute) throws InvalidPrefixException {

        Set<QName> qNames = new HashSet<QName>();

        String val = reader.getAttributeValue(null, attribute);
        if (val != null) {
            StringTokenizer tok = new StringTokenizer(val);
            while (tok.hasMoreElements()) {
                qNames.add(StaxUtil.createQName(tok.nextToken(), reader));
            }
        }

        return qNames;

    }

    /**
     * Serializes the infoset in the stream reader.
     *
     * @param reader Stream reader.
     * @return Serialized XML.
     * @throws XMLStreamException In case of an xml stream error.
     */
    public static String serialize(XMLStreamReader reader) throws XMLStreamException {

        try {

            StringBuffer xml = new StringBuffer();

            int event = reader.getEventType();
            while (true) {

                switch (event) {
                case XMLStreamConstants.START_ELEMENT:
                    onStartElement(reader, xml);
                    onNsMappings(reader, xml);
                    onAttributes(reader, xml);
                    xml.append(">");
                    break;
                case XMLStreamConstants.CHARACTERS:
                    if (reader.isWhiteSpace()) {
                        break;
                    }
                    xml.append(reader.getText());
                    break;
                case XMLStreamConstants.END_ELEMENT:
                    onEndElement(reader, xml);
                    break;
                }

                if (!reader.hasNext()) {
                    break;
                }
                event = reader.next();

            }
            return xml.toString();

        } finally {
            reader.close();
        }

    }

    /**
     * Constructs a QName from the given name. If a namespace prefix is not specified in the name, the namespace context is used
     *
     * @param name   the name to parse
     * @param reader the XML stream reader
     * @return the parsed QName
     * @throws InvalidPrefixException if a specified namespace prefix is invalid
     */
    public static QName createQName(String name, XMLStreamReader reader) throws InvalidPrefixException {
        QName qName;
        int index = name.indexOf(':');
        if (index != -1) {
            String prefix = name.substring(0, index);
            String localPart = name.substring(index + 1);
            String ns = reader.getNamespaceContext().getNamespaceURI(prefix);
            if (ns == null) {
                throw new InvalidPrefixException("Invalid prefix: " + prefix, prefix);
            }
            qName = new QName(ns, localPart, prefix);
        } else {
            String prefix = "";
            String ns = reader.getNamespaceURI();
            qName = new QName(ns, name, prefix);
        }
        return qName;
    }

    /*
     * Renders end element markup.
     */
    private static void onEndElement(XMLStreamReader reader, StringBuffer xml) {
        String name = getName(reader);
        xml.append("</");
        xml.append(name);
        xml.append(">");
    }

    /*
     * Gets the fully-qualified name of the element.
     */
    private static String getName(XMLStreamReader reader) {
        QName qname = reader.getName();
        String namePrefix = qname.getPrefix();
        String localPart = qname.getLocalPart();
        return namePrefix == null || "".equals(namePrefix) ? localPart : namePrefix + ":" + localPart;
    }

    /*
     * Render the attributes.
     */
    private static void onAttributes(XMLStreamReader reader, StringBuffer xml) {
        int n = reader.getAttributeCount();
        for (int i = 0; i < n; ++i) {
            xml.append(' ');
            xml.append(reader.getAttributeLocalName(i));
            xml.append('=');
            xml.append('\'');
            xml.append(reader.getAttributeValue(i));
            xml.append('\'');
        }
    }

    /*
     * Renedr namespace mappings.
     */
    private static void onNsMappings(XMLStreamReader reader, StringBuffer xml) {
        int n = reader.getNamespaceCount();
        for (int i = 0; i < n; ++i) {
            String prefix = reader.getNamespacePrefix(i);
            xml.append(" xmlns");
            if (prefix != null) {
                xml.append(':').append(prefix);
            }
            xml.append('=');
            xml.append('\'');
            xml.append(reader.getNamespaceURI(i));
            xml.append('\'');
        }
    }

    /*
     * Render start element.
     */
    private static void onStartElement(XMLStreamReader reader, StringBuffer xml) {
        xml.append("<");
        String name = getName(reader);
        xml.append(name);
    }


}
