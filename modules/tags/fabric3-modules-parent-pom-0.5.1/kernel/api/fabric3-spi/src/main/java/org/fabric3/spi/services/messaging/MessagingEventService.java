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
package org.fabric3.spi.services.messaging;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * @version $Rev$ $Date$
 */
public interface MessagingEventService {

    /**
     * Registers a request listener for async messages. Request listeners handle unslolicited async messages sent by
     * recipients.
     *
     * @param messageType Message type that can be handled by the listener.
     * @param listener    Recipient of the async message.
     */
    void registerRequestListener(QName messageType, RequestListener listener);

    /**
     * Un registers a request listener for async messages.
     *
     * @param messageType Message type that can be handled by the listener.
     */
    void unRegisterRequestListener(QName messageType);

    /**
     * Dispatches an event to a registered listener for the message type. If no listener is found, the message is
     * ignored.
     *
     * @param messageType the message type
     * @param content     the message body
     */
    void publish(QName messageType, XMLStreamReader content);

}
