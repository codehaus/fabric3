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
package org.fabric3.binding.ejb.runtime;

import java.net.URI;
import org.fabric3.spi.builder.WiringException;

/**
 * Various application servers may have different mechanisms for resolving EJBs.
 * This interface is intended to allow users to hook into a particular app server's
 * resolution mechanism.
 *
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public interface EjbRegistry {

    public Object resolveEjbLink(String ejbLink, Class<?> interfaceClass) throws WiringException;
    public void registerEjbLink(String ejbLink, Object ejb) throws WiringException;

    public Object resolveEjb(URI uri) throws WiringException;
    public void registerEjb(URI uri, Object ejb) throws WiringException;

}
