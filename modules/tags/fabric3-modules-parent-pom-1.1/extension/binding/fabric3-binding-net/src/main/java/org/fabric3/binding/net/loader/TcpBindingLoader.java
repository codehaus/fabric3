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
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.binding.net.config.TcpConfig;
import org.fabric3.binding.net.model.TcpBindingDefinition;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.InvalidValue;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * Loader for binding.http.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TcpBindingLoader extends AbstractBindingLoader<TcpBindingDefinition> {
    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public TcpBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public TcpBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        URI uri = parseUri(reader, context);
        String scheme = uri.getScheme();
        if (scheme != null && !"tcp".equalsIgnoreCase(scheme)) {
            InvalidValue failure = new InvalidValue("Absolute binding URIs must use TCP as the scheme", reader);
            context.addError(failure);
        }
        Document key = loaderHelper.loadKey(reader);
        TcpBindingDefinition definition = new TcpBindingDefinition(uri, key);
        TcpConfig config = definition.getConfig();
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
        parseBindingAttributes(reader, config, context);
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.END_ELEMENT:
                if ("binding.tcp".equals(reader.getName().getLocalPart())) {
                    String wireFormat = definition.getConfig().getWireFormat();
                    if (wireFormat != null && !"jdk".equals(wireFormat)) {
                        // record the wire format requirement so the extension can be provisioned
                        definition.addRequiredCapability(wireFormat);
                    }
                    return definition;
                }
                break;
            case XMLStreamConstants.START_ELEMENT:
                String name = reader.getName().getLocalPart();
                if (name.startsWith("wireFormat.")) {
                    parseWireFormat(reader, config, false, context);
                } else if ("response".equals(name)) {
                    parseResponse(reader, config, context);
                } else if ("sslSettings".equals(name)) {
                    parseSslSettings(reader, config, context);
                }
                break;
            }

        }
    }

}