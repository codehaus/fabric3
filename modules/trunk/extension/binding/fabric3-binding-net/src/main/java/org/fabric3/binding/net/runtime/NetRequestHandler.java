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

import org.fabric3.spi.wire.Wire;

/**
 * Receives requests from a channel and passes them to the appropriate wire invocation chain.
 *
 * @version $Revision$ $Date$
 */
public interface NetRequestHandler {

    /**
     * Registers a wire for a request path, i.e. the path of the service URI.
     *
     * @param path        the path part of the service URI
     * @param callbackUri the callback URI associated with the wire or null if it is unidirectional
     * @param wire        the wire
     */
    void register(String path, String callbackUri, Wire wire);

    /**
     * Unregisters a wire for a request path, i.e. the path of the service URI.
     *
     * @param path the path part of the service URI
     */
    void unregister(String path);
}
