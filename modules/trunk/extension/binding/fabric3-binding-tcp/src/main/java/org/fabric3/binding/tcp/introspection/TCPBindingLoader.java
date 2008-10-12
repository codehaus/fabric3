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
package org.fabric3.binding.tcp.introspection;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.binding.tcp.scdl.TCPBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Revision$ $Date$
 */
public class TCPBindingLoader implements TypeLoader<TCPBindingDefinition> {

    /**
     * {@inheritDoc}
     */
    public TCPBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext)
            throws XMLStreamException {

        TCPBindingDefinition bd = null;
        String uri = null;

        try {

            uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", "uri", reader);
                introspectionContext.addError(failure);
                return null;
            }
            if (!uri.contains("://")) { // Default to TCP.
                uri = "tcp://" + uri;
            }
            String key = reader.getAttributeValue(null, "key");

            bd = new TCPBindingDefinition(new URI(uri), key);

        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The TCP binding URI is not valid: " + uri, "uri", reader);
            introspectionContext.addError(failure);
        }

        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

}
