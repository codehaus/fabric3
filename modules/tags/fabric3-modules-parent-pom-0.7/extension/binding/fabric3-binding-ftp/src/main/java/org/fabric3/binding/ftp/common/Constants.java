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
package org.fabric3.binding.ftp.common;

import javax.xml.namespace.QName;

import org.fabric3.spi.Namespaces;

/**
 * @version $Revision$ $Date$
 */
public interface Constants {

    /**
     * Qualified name for the binding element.
     */
    QName BINDING_QNAME = new QName(Namespaces.BINDING, "binding.ftp");

    /**
     * Qualified name for the policy element.
     */
    QName POLICY_QNAME = new QName(Namespaces.POLICY, "security");

    /**
     * The value for specifying no timeout for blocking operations on an FTP socket.
     */
    int NO_TIMEOUT = 0;
}
