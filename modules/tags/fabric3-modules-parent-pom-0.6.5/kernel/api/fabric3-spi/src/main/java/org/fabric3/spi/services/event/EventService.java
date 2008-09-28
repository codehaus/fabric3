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
