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
package org.fabric3.jaxb.provision;

import java.util.Set;
import javax.xml.namespace.QName;

/**
 * Definition for an interceptor that transforms a JAXB object to an XML string representation.
 *
 * @version $Revision$ $Date$
 */
public class ReferenceTransformingInterceptorDefinition extends AbstractTransformingInterceptorDefinition {
    private static final long serialVersionUID = 4866557038407403754L;

    public ReferenceTransformingInterceptorDefinition(QName dataType, Set<String> classNames) {
        super(dataType, classNames);
    }
}