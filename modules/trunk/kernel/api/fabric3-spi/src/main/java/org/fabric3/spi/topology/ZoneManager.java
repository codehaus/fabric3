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

import java.util.List;

/**
 * @version $Revision$ $Date$
 */
public interface ZoneManager {

    /**
     * Returns true if the runtime is acting as a zone mamager.
     *
     * @return true if the runtime is acting as a zone mamager
     */
    public boolean isZoneManager();

    /**
     * Returns a list of active RuntimeInstances in the zone.
     *
     * @return the RuntimeInstances  active in the zone.
     */
    List<RuntimeInstance> getRuntimes();

    /**
     * Sends a message to the given runtime.
     *
     * @param runtimeName the runtime name to send the message to
     * @param message     the serialized message
     * @throws MessageException if an error occurs sending the message
     */
    void sendMessage(String runtimeName, byte[] message) throws MessageException;

}