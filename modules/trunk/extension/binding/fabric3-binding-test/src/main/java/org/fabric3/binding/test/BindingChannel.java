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
package org.fabric3.binding.test;

import java.net.URI;

import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.wire.Wire;

/**
 * Implementations route messages to a service destination.
 *
 * @version $Rev$ $Date$
 */
public interface BindingChannel {

    /**
     * Registers a wire to a service destination
     *
     * @param uri  the destination uri
     * @param wire the wire
     * @param callbackUri the callback uri or null
     */
    void registerDestinationWire(URI uri, Wire wire, URI callbackUri);

    /**
     * Sends a message to the destination, invoking the given operation. Note overloaded operations are not supported
     *
     * @param destination the destination uri
     * @param operation   the operation name
     * @param msg         the message
     * @return the response
     */
    Message send(URI destination, String operation, Message msg);

}
