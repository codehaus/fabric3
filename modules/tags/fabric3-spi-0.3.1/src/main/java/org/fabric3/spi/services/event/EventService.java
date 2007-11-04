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
package org.fabric3.spi.services.event;

/**
 * The runtime event service. {@link Fabric3EventListener}s subscribe with this service to receive notification of
 * various runtime events.
 *
 * @version $Rev$ $Date$
 */
public interface EventService {

    /**
     * Publishes a runtime event. EventListeners subscribed to the event will be notified.
     *
     * @param event the event
     */
    <T extends Fabric3Event> void publish(T event);

    /**
     * Subscribes the listener to receive notification when events of the specified type are published.
     *
     * @param type     the event type to receive notifications for
     * @param listener the listener to subscribe
     */
    <T extends Fabric3Event> void subscribe(Class<T> type, Fabric3EventListener listener);

    /**
     * Unsubscribes the listener from receiving notifications when events of the specified type are published.
     *
     * @param type     the event type to unsibscribe from
     * @param listener the listener to unsubscribe
     */
    <T extends Fabric3Event> void unsubscribe(Class<T> type, Fabric3EventListener listener);


}
