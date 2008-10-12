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
package org.fabric3.federation.shoal;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;

/**
 * Constants for federation extension classes.
 *
 * @version $Revision$ $Date$
 */
public interface FederationConstants {

    /**
     * The name of the DomainManager service in a Shoal domain group. The is one DomainManager instance per domain.
     */
    String DOMAIN_MANAGER = "DomainManager";

    /**
     * The name of the ZoneManager service in a Shoal zone group. There is one ZoneManager instance per zone.
     */
    String ZONE_MANAGER = "ZoneManager";

    /**
     * The name of the RuntimeManager service in a Shoal zone group. There is a RuntimeManager instance per zone participant.
     */
    String RUNTIME_MANAGER = "RuntimeManager";

    /**
     * The key for zone transport metadata.
     */
    QName ZONE_TRANSPORT_INFO = new QName(Constants.FABRIC3_NS, "ZoneTransportInfo");

}
