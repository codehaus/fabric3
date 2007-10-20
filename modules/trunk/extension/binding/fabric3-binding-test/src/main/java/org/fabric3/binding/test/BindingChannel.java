/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.binding.test;

import java.net.URI;

import org.fabric3.spi.wire.Message;
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
     */
    void registerDestinationWire(URI uri, Wire wire);

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
