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
package org.fabric3.binding.test;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.spi.Namespaces;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Parses <code>binding.test</code> for services and references. A uri to bind the service to or target a reference must be provided as an attribute.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TestBindingLoader implements TypeLoader<TestBindingDefinition> {

    public static final QName BINDING_QNAME = new QName(Namespaces.BINDING, "binding.test");

    private final LoaderHelper loaderHelper;

    /**
     * Constructor.
     *
     * @param loaderHelper the policy helper
     */
    public TestBindingLoader(@Reference LoaderHelper loaderHelper) {
        this.loaderHelper = loaderHelper;
    }

    public TestBindingDefinition load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        TestBindingDefinition definition = null;
        String uri = null;
        try {
            uri = reader.getAttributeValue(null, "uri");
            if (uri == null) {
                MissingAttribute failure = new MissingAttribute("The uri attribute is not specified", "uri", reader);
                context.addError(failure);
                return null;
            } else {
                definition = new TestBindingDefinition(new URI(uri), loaderHelper.loadKey(reader));
            }
        } catch (URISyntaxException ex) {
            InvalidValue failure = new InvalidValue("The Burlap binding URI is not valid: " + uri, "uri", reader);
            context.addError(failure);
        }
        LoaderUtil.skipToEndElement(reader);
        return definition;

    }

}
