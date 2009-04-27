/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.binding.net.loader;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Document;

import org.fabric3.binding.net.config.HttpConfig;
import org.fabric3.binding.net.model.HttpBindingDefinition;
import org.fabric3.host.Namespaces;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.MissingAttribute;

/**
 * Loader for binding.http.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HttpBindingLoader extends AbstractBindingLoader<HttpBindingDefinition> {
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
        HttpConfig config = definition.getConfig();
        loaderHelper.loadPolicySetsAndIntents(definition, reader, context);
        parseBindingAttributes(reader, config, context);
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
                    parseWireFormat(reader, config, false, context);
                } else if ("response".equals(name)) {
                    parseResponse(reader, config, context);
                } else if ("sslSettings".equals(name)) {
                    parseSslSettings(reader, config, context);
                } else if ("authentication".equals(name)) {
                    parseAuthentication(reader, config, context);
                }
                break;
            }

        }
    }

    private void parseAuthentication(XMLStreamReader reader, HttpConfig config, IntrospectionContext context) {
        String auth = reader.getAttributeValue(null, "type");
        if (auth == null) {
            MissingAttribute failure = new MissingAttribute("An authentication type must be specified ", reader);
            context.addError(failure);
            return;
        }
        config.setAuthenticationType(auth);

    }

}
