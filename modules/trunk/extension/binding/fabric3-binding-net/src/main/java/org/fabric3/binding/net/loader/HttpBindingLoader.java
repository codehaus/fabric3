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
package org.fabric3.binding.net.loader;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * @version $Revision$ $Date$
 */
public class HttpBindingLoader implements TypeLoader<HttpBindingDefinition> {
    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public HttpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public HttpBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        URI uri = parseUri(reader, context);
        Document key = loaderHelper.loadKey(reader);
        HttpBindingDefinition definition = new HttpBindingDefinition(uri, key);

        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
        parseBindingAttributes(reader, definition, context);
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.END_ELEMENT:
                if ("binding.http".equals(reader.getName().getLocalPart())) {
                    return definition;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                String name = reader.getName().getLocalPart();
                if ("wireFormat".equals(name)) {
                    parseWireFormat(reader, definition, false, context);
                } else if ("responseWireFormat".equals(name)) {
                    parseWireFormat(reader, definition, true, context);
                } else if ("sslSettings".equals(name)) {
                    parseSslSettings(reader, definition, context);
                } else if ("authentication".equals(name)) {
                    parseAuthentication(reader, definition, context);
                }
                break;
            }

        }
    }

    private URI parseUri(XMLStreamReader reader, IntrospectionContext context) {
        String uriString = reader.getAttributeValue(null, "uri");
        if (uriString == null) {
            MissingAttribute failure = new MissingAttribute("A binding URI must be specified ", reader);
            context.addError(failure);
            return null;
        }

        try {
            return new URI(uriString);
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The HTTP binding URI is not valid: " + uriString, reader);
            context.addError(failure);
            return null;
        }
    }

    private void parseBindingAttributes(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) {
        String readTimeout = reader.getAttributeValue(null, "readTimeout");
        definition.setReadTimeout(readTimeout);
        String numberOfRetries = reader.getAttributeValue(null, "numberOfRetries");
        try {
            definition.setNumberOfRetries(Integer.parseInt(numberOfRetries));
        } catch (NumberFormatException e) {
            InvalidValue failure = new InvalidValue("Invalid number of retries value ", reader);
            context.addError(failure);
        }
        // validate
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"readTimeout".equals(name) || !"numberOfRetries".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }

    }

    private void parseWireFormat(XMLStreamReader reader, HttpBindingDefinition definition, boolean response, IntrospectionContext context) {
        String type = reader.getAttributeValue(null, "type");
        if (type == null) {
            MissingAttribute failure = new MissingAttribute("A wire format type must be specified ", reader);
            context.addError(failure);
            return;
        }
        if (response) {
            definition.setResponseWireFormat(type);
        } else {
            definition.setWireFormat(type);
        }

    }

    private void parseSslSettings(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) {
        String alias = reader.getAttributeValue(null, "alias");
        if (alias == null) {
            MissingAttribute failure = new MissingAttribute("An SSL alias must be specified ", reader);
            context.addError(failure);
            return;
        }
        definition.setSslSettings(alias);

    }

    private void parseAuthentication(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) {
        String auth = reader.getAttributeValue(null, "type");
        if (auth == null) {
            MissingAttribute failure = new MissingAttribute("An authentication type must be specified ", reader);
            context.addError(failure);
            return;
        }
        definition.setAuthenticationType(auth);

    }

    private static final String XML = "<binding.http readTimeout='10000' numberOfRetries='1' uri ='TestComponent'>" +
            "<wireFormat type='testFormat'/>" +
            "<responseWireFormat type='testFormat'/>" +
            "<sslSettings alias='sslSettings'/>" +
            "<authentication type='basic'/>" +
            "</binding.http>";

}
