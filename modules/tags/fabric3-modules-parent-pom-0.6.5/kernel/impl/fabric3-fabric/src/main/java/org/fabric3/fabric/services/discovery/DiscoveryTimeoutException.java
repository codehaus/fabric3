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
package org.fabric3.fabric.services.discovery;

import org.fabric3.spi.services.discovery.DiscoveryException;

/**
 * Denotes a timeout while performing a discovery operation.
 *
 * @version $Rev$ $Date$
 */
public class DiscoveryTimeoutException extends DiscoveryException {
    private static final long serialVersionUID = 7156621402105248064L;

    protected DiscoveryTimeoutException(String message) {
        super(message, null);
    }
}
