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
package org.fabric3.binding.ejb.introspection;

import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.InvalidValue;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbBindingLoader implements TypeLoader<EjbBindingDefinition> {

    /**
     * Qualified name for the binding element.
     */
    public static final QName BINDING_QNAME = new QName(SCA_NS, "binding.ejb");

    public EjbBindingDefinition load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        String uri = reader.getAttributeValue(null, "uri");

        URI targetUri;
        try {
            targetUri = createURI(uri);
        } catch (URISyntaxException e) {
            InvalidValue failure = new InvalidValue("Invalid EJB binding URI: " + uri, "uri", reader);
            introspectionContext.addError(failure);
            return null;
        }
        String key = reader.getAttributeValue(null, "key");
        EjbBindingDefinition bd = new EjbBindingDefinition(targetUri, key);

        String homeInterface = reader.getAttributeValue(null, "homeInterface");
        bd.setHomeInterface(homeInterface);

        bd.setEjbLink(reader.getAttributeValue(null, "ejb-link-name"));

        if ("stateful".equalsIgnoreCase(reader.getAttributeValue(null, "session-type"))) {
            bd.setStateless(false);
        }

        boolean isEjb3;
        String ejbVersion = reader.getAttributeValue(null, "ejb-version");
        if (ejbVersion != null) {
            isEjb3 = "EJB3".equalsIgnoreCase(ejbVersion);
        } else {
            isEjb3 = (homeInterface == null);
        }
        bd.setEjb3(isEjb3);

        if (!isEjb3 && homeInterface == null) {
            MissingAttribute failure = new MissingAttribute("homeInterface must be specified for EJB 2.x bindings", "homeInterface", reader);
            introspectionContext.addError(failure);
        }

        bd.setName(reader.getAttributeValue(null, "name"));


        LoaderUtil.skipToEndElement(reader);
        return bd;

    }

    private URI createURI(String uri) throws URISyntaxException {
        if (uri == null) return null;

        // In EJB 3, the @Stateless & @Stateful annotations contain an attribute named mappedName.
        // Although the specification doesn't spell out what this attribute is used for, it is
        // commonly used to specify a JNDI name for the EJB.  However, EJB 3 beans can have multiple
        // interfaces.  As a result, most containers including Glassfish and WebLogic calculate a JNDI
        // name for each interface based on the mappedName.  In both Glassfish and WebLogic, the JNDI
        // name for each interface is calculated using the following formula:
        // <mappedName>#<fully qualified interface name>
        // The problem is that the '#' char is a URI fragment delimitor and therefore can't legally be used
        // in a URI.  Constructing a URI from such a JNDI name leads to an URISyntaxException being thrown.
        // As such, we'll attempt to account for this issue by stripping off the "corbaname:rir:#" portion
        // of the URI string before we actually construct the URI object.

        if (uri.indexOf('#') != uri.lastIndexOf('#')) {
            if (uri.startsWith("corbaname:rir:#")) {
                uri = uri.substring(uri.indexOf('#') + 1);
            }
        }

        return new URI(uri);
    }

}
