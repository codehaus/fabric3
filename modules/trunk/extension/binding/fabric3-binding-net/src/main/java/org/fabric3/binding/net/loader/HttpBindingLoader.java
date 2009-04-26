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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader for binding.http.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HttpBindingLoader implements TypeLoader<HttpBindingDefinition> {
    private final LoaderHelper loaderHelper;
    private static final QName JAXB_POLICY = new QName(Namespaces.POLICY, "dataBinding.jaxb");

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
                    String wireFormat = definition.getConfig().getWireFormat();
                    if (wireFormat == null) {
                        // make JSON the default serialization
                        // TODO make this configurable
                        definition.addIntent(JAXB_POLICY);
                    } else {
                        definition.addIntent(new QName(Namespaces.POLICY, "dataBinding." + wireFormat));
                    }
                    return definition;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                String name = reader.getName().getLocalPart();
                if (name.startsWith("wireFormat.")) {
                    parseWireFormat(reader, definition, false, context);
                } else if ("response".equals(name)) {
                    parseResponse(reader, definition, context);
                } else if ("sslSettings".equals(name)) {
                    parseSslSettings(reader, definition, context);
                } else if ("authentication".equals(name)) {
                    parseAuthentication(reader, definition, context);
                }
                break;
            }

        }
    }

    private void parseResponse(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if (reader.getName().getLocalPart().startsWith("wireFormat.")) {
                    parseWireFormat(reader, definition, true, context);
                    break;
                }
            case XMLStreamConstants.END_ELEMENT:
                if ("response".equals(reader.getName().getLocalPart())) {
                    return;
                }
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
        if (readTimeout != null) {
            definition.getConfig().setReadTimeout(readTimeout);
        }
        String numberOfRetries = reader.getAttributeValue(null, "numberOfRetries");
        if (numberOfRetries != null) {
            try {
                definition.getConfig().setNumberOfRetries(Integer.parseInt(numberOfRetries));
            } catch (NumberFormatException e) {
                InvalidValue failure = new InvalidValue("Invalid number of retries value ", reader);
                context.addError(failure);
            }
        }
        // validate
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"uri".equals(name) && !"readTimeout".equals(name) && !"numberOfRetries".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }

    }

    private void parseWireFormat(XMLStreamReader reader, HttpBindingDefinition definition, boolean response, IntrospectionContext context) {
        String name = reader.getName().getLocalPart();
        if (name.length() < 11) {
            InvalidWireFormat failure = new InvalidWireFormat("Invalid wire format: " + name, reader);
            context.addError(failure);
            return;
        }
        String format = name.substring(11); //wireFormat.

        if (response) {
            definition.getConfig().setResponseWireFormat(format);
        } else {
            definition.getConfig().setWireFormat(format);
        }

    }

    private void parseSslSettings(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) {
        String alias = reader.getAttributeValue(null, "alias");
        if (alias == null) {
            MissingAttribute failure = new MissingAttribute("An SSL alias must be specified ", reader);
            context.addError(failure);
            return;
        }
        definition.getConfig().setSslSettings(alias);

    }

    private void parseAuthentication(XMLStreamReader reader, HttpBindingDefinition definition, IntrospectionContext context) {
        String auth = reader.getAttributeValue(null, "type");
        if (auth == null) {
            MissingAttribute failure = new MissingAttribute("An authentication type must be specified ", reader);
            context.addError(failure);
            return;
        }
        definition.getConfig().setAuthenticationType(auth);

    }

}
