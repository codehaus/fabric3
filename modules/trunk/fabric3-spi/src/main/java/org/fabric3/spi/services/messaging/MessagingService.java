/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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

import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

/**
 * Defines the abstraction allowing runtimes to exchange arbitrary messages with each other.
 *
 * @version $Revision$ $Date$
 */
public interface MessagingService {
    /**
     * Attempts to Join the domain, timeing out after the specified wait.
     *
     * @param waitTime the time to wait before timing out or -1 to wait indefinitely
     * @throws DomainJoinException       if an error occurs joining the domain
     * @throws MessagingTimeoutException if a timeout occurs joining the domain
     */

    void joinDomain(long waitTime) throws DomainJoinException, MessagingTimeoutException;

    /**
     * Detaches the node from the domain
     *
     * @throws MessagingException if an error occurs leaving the domain
     */
    void leaveDomain() throws MessagingException;

    /**
     * Sends a message to the specified runtime. The method returns a unique message id for the sent message. The
     * consumers can use the message id for correlating responses to sent messages.
     *
     * @param runtimeId Runtime id of recipient.
     * @param content   Message content.
     * @return The message id.
     * @throws MessagingException In case of discovery errors.
     */
    int sendMessage(String runtimeId, XMLStreamReader content) throws MessagingException;

    /**
     * Broadcasts the messages to all runtimes in the domain.
     *
     * @param content Message content.
     * @return The message id.
     * @throws MessagingException In case of discovery errors.
     */
    int broadcastMessage(XMLStreamReader content) throws MessagingException;

    /**
     * Registers a request listener for async messages. Request listeners handle unslolicited async messages sent by
     * recipients.
     *
     * @param messageType Message type that can be handled by the listener.
     * @param listener    Recipient of the async message.
     */
    void registerRequestListener(QName messageType, RequestListener listener);

    /**
     * Registers a response listener for async messages. Response listeners handle async meesages that are received in
     * response to a request message that was originally sent.
     *
     * @param messageType Message type that can be handled by the listener.
     * @param listener    Recipient of the async message.
     */
    void registerResponseListener(QName messageType, ResponseListener listener);

    /**
     * Returns the available runtimes in the current domain.
     *
     * @return List of available runtimes.
     */
    Set<String> getRuntimeIds();

}
