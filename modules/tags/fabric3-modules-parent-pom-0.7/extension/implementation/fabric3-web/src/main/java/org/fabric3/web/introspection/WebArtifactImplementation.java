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

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.Namespaces;

/**
 * Represents the implementation of a web artifact such as a servlet or filter.
 *
 * @version $Revision$ $Date$
 */
public class WebArtifactImplementation extends Implementation<PojoComponentType> {
    private static final long serialVersionUID = -5415465119697665067L;
    public static final QName QNAME = new QName(Namespaces.IMPLEMENTATION, "webArtifact");

    public QName getType() {
        return QNAME;
    }
}
