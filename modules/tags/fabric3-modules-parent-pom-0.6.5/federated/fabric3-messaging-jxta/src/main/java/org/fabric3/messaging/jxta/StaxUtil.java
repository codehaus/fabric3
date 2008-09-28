/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ÒLicenseÓ), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an Òas isÓ basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.messaging.jxta;

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
