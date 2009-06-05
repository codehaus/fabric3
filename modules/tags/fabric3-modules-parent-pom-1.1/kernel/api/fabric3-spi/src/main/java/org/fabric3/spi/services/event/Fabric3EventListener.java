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
 * Implementations are notified of runtime events after they have subscribed with the {@link EventService} for a
 * particular event type or types.
 *
 * @version $Rev$ $Date$
 */
public interface Fabric3EventListener<T extends Fabric3Event> {

    /**
     * Notifies the listener of an event. The listener must not throw an exception as all listeners are notified on the
     * same thread.
     *
     * @param event the event
     */
    void onEvent(T event);

}
