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
package org.fabric3.web.introspection;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Implementation;
import org.fabric3.spi.Namespaces;

/**
 * Model object for a web component.
 *
 * @version $Rev: 956 $ $Date: 2007-08-31 15:35:28 -0700 (Fri, 31 Aug 2007) $
 */
public class WebImplementation extends Implementation<WebComponentType> {
    private static final long serialVersionUID = 5589199308230767243L;
    // the deprecated, F3-specific namespace
    @Deprecated
    public static final QName IMPLEMENTATION_WEBAPP = new QName(Namespaces.IMPLEMENTATION, "web");
    public static final QName IMPLEMENTATION_WEB = new QName(org.osoa.sca.Constants.SCA_NS, "implementation.web");

    public QName getType() {
        return IMPLEMENTATION_WEB;
    }

}
