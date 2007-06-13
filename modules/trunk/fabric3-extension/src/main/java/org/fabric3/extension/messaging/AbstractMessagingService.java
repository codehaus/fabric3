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
package org.fabric3.extension.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.messaging.MessagingException;
import org.fabric3.spi.services.messaging.MessagingService;
import org.fabric3.spi.services.messaging.RequestListener;
import org.fabric3.spi.services.messaging.ResponseListener;

/**
 * Abstract implementation of the discovery service.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public abstract class AbstractMessagingService implements MessagingService {

    /**
     * Runtime info.
     */
    private HostInfo hostInfo;

    /**
     * Request listeners.
     */
    private Map<QName, RequestListener> requestListenerMap = new ConcurrentHashMap<QName, RequestListener>();

    /**
     * Response listeners.
     */
    private Map<QName, ResponseListener> responseListenerMap = new ConcurrentHashMap<QName, ResponseListener>();

    /**
     * Registers a request listener for async messages.
     *
     * @param messageType Message type that can be handled by the listener.
     * @param listener    Recipient of the async message.
     */
    public void registerRequestListener(QName messageType, RequestListener listener) {
        requestListenerMap.put(messageType, listener);
    }

    /**
     * Registers a response listener for async messages.
     *
     * @param messageType Message type that can be handled by the listener.
     * @param listener    Recipient of the async message.
     */
    public void registerResponseListener(QName messageType, ResponseListener listener) {
        responseListenerMap.put(messageType, listener);
    }

    /**
     * Sets the runtime info for the runtime using the discovery service.
     *
     * @param hostInfo Runtime info for the runtime using the discovery service.
     */
    @Reference
    public final void setRuntimeInfo(HostInfo hostInfo) {
        this.hostInfo = hostInfo;
    }

    /**
     * Gets the runtime info for the runtime using the discovery service.
     *
     * @return Runtime info for the runtime using the discovery service.
     */
    protected final HostInfo getRuntimeInfo() {
        return hostInfo;
    }

    /**
     * Returns the request listener for the specified message type.
     *
     * @param messageType Message type for the incoming message.
     * @return Listener interested in the message type.
     */
    public final RequestListener getRequestListener(QName messageType) {
        return requestListenerMap.get(messageType);
    }

    /**
     * Returns the request listener for the specified message type.
     *
     * @param messageType Message type for the incoming message.
     * @return Listener interested in the message type.
     */
    public final ResponseListener getResponseListener(QName messageType) {
        return responseListenerMap.get(messageType);
    }

    /**
     * Broadcasts the messages to all runtimes in the domain.
     *
     * @param content Message content.
     * @return The message id.
     * @throws MessagingException In case of discovery errors.
     */
    public int broadcastMessage(XMLStreamReader content) throws MessagingException {
        return sendMessage(null, content);
    }

}
