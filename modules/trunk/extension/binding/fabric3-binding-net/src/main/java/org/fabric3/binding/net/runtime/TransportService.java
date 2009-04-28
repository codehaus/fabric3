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
package org.fabric3.binding.net.runtime;

import org.fabric3.binding.net.provision.TransportType;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.wire.Wire;

/**
 * Registers wires for services with an transport channel.
 *
 * @version $Revision$ $Date$
 */
public interface TransportService {

    /**
     * Register the wire.
     *
     * @param type        the transport channel type
     * @param path        the service path which is its relative URI
     * @param callbackUri the callback URI or null if the service is unidirectional
     * @param wire        the wire
     * @throws WiringException if an exception registering the wire occurs
     */
    void register(TransportType type, String path, String callbackUri, Wire wire) throws WiringException;

    /**
     * Unregister the wire.
     *
     * @param type the transport channel type
     * @param path the service path which is its relative URI
     */
    void unregister(TransportType type, String path);

}
