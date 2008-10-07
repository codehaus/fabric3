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
package org.fabric3.spi.topology;

import java.io.Serializable;
import java.util.List;
import javax.xml.namespace.QName;

/**
 * Coordinates federated communications accross a domain. DomainManagers are associated with controller instances for a domain. A domain topology
 * consists of:
 * <pre>
 * <ul>
 * </ul>
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public interface DomainManager {

    /**
     * Returns the zones active in the domain.
     *
     * @return the zones active in the domain
     */
    List<Zone> getZones();

    /**
     * Returns the transport metadata for a given zone and transport type.
     *
     * @param zone      the zone name
     * @param type      the metadata type
     * @param transport the transport type
     * @return the opaque metadata
     */
    <T extends Serializable> T getTransportMetaData(String zone, Class<T> type, QName transport);

    /**
     * Sends a message to the given zone manager.
     *
     * @param zoneName the zone name to send the message to
     * @param message  the serialized message
     * @throws MessageException if an error occurs sending the message
     */
    void sendMessage(String zoneName, byte[] message) throws MessageException;

}
