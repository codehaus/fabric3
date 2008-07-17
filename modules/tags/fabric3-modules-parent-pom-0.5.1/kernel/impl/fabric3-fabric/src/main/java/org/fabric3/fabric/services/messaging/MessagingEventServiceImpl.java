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
package org.fabric3.fabric.services.messaging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.fabric3.spi.services.messaging.MessagingEventService;
import org.fabric3.spi.services.messaging.RequestListener;

/**
 * MessagingEventService implementation.
 *
 * @version $Rev$ $Date$
 */
public class MessagingEventServiceImpl implements MessagingEventService {
    private Map<QName, RequestListener> cache = new ConcurrentHashMap<QName, RequestListener>();

    public void registerRequestListener(QName messageType, RequestListener listener) {
        cache.put(messageType, listener);
    }

    public void unRegisterRequestListener(QName messageType) {
        cache.remove(messageType);
    }

    public void publish(QName messageType, XMLStreamReader content) {
        RequestListener listener = cache.get(messageType);
        if (listener == null) {
            // ignore th message
            return;
        }
        listener.onRequest(content);
    }
}
