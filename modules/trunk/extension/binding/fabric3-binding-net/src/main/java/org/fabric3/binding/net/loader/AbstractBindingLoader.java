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

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.net.config.BaseConfig;
import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loader for binding.http.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public abstract class AbstractBindingLoader<T extends BindingDefinition> implements TypeLoader<T> {

    protected void parseResponse(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) throws XMLStreamException {
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if (reader.getName().getLocalPart().startsWith("wireFormat.")) {
                    parseWireFormat(reader, config, true, context);
                    break;
                }
            case XMLStreamConstants.END_ELEMENT:
                if ("response".equals(reader.getName().getLocalPart())) {
                    return;
                }
            }
        }
    }

    protected URI parseUri(XMLStreamReader reader, IntrospectionContext context) {
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

    protected void parseBindingAttributes(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) {
        String readTimeout = reader.getAttributeValue(null, "readTimeout");
        if (readTimeout != null) {
            try {
                long timeout = Long.parseLong(readTimeout);
                config.setReadTimeout(timeout);
            } catch (NumberFormatException e) {
                context.addError(new InvalidValue("Invalid timeout: " + readTimeout, reader, e));
            }
        }
        String numberOfRetries = reader.getAttributeValue(null, "numberOfRetries");
        if (numberOfRetries != null) {
            try {
                config.setNumberOfRetries(Integer.parseInt(numberOfRetries));
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

    protected void parseWireFormat(XMLStreamReader reader, BaseConfig config, boolean response, IntrospectionContext context) {
        String name = reader.getName().getLocalPart();
        if (name.length() < 11) {
            InvalidWireFormat failure = new InvalidWireFormat("Invalid wire format: " + name, reader);
            context.addError(failure);
            return;
        }
        String format = name.substring(11); //wireFormat.

        if (response) {
            config.setResponseWireFormat(format);
        } else {
            config.setWireFormat(format);
        }

    }

    protected void parseSslSettings(XMLStreamReader reader, BaseConfig config, IntrospectionContext context) {
        String alias = reader.getAttributeValue(null, "alias");
        if (alias == null) {
            MissingAttribute failure = new MissingAttribute("An SSL alias must be specified ", reader);
            context.addError(failure);
            return;
        }
        config.setSslSettings(alias);

    }

}