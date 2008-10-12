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
package org.fabric3.binding.rmi.model.logical;

import java.net.URI;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;

@EagerInit
public class RmiBindingLoader implements TypeLoader<RmiBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(SCA_NS, "binding.rmi");

    public RmiBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        String uri = reader.getAttributeValue(null, "uri");
        String name = reader.getAttributeValue(null, "name");
        assert name != null && name.length() > 0;
        String serviceName = reader.getAttributeValue(null, "serviceName");
        URI targetURI;
        if (uri != null) {
            targetURI = URI.create(uri);
        } else {
            String target = serviceName != null ? serviceName : name;
            targetURI = URI.create(target);
        }

        String key = reader.getAttributeValue(null, "key");
        RmiBindingDefinition definition = new RmiBindingDefinition(targetURI, key);
        definition.setName(name);
        if (serviceName != null) {
            definition.setServiceName(serviceName);
        } else {
            definition.setServiceName(name);
        }
        String attribute = reader.getAttributeValue(null, "host");
        if (attribute != null) {
            definition.setHost(attribute);
        }
        attribute = reader.getAttributeValue(null, "port");
        if (attribute != null) {
            definition.setPort(Integer.parseInt(attribute));
        }
        LoaderUtil.skipToEndElement(reader);
        return definition;
    }

}
