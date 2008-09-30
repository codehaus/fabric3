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
package org.fabric3.fabric.services.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3Event;
import org.fabric3.spi.services.event.Fabric3EventListener;

/**
 * Default implementation of the EventService.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class EventServiceImpl implements EventService {
    private Map<Class<Fabric3Event>, List<Fabric3EventListener<Fabric3Event>>> cache;

    public EventServiceImpl() {
        cache = new ConcurrentHashMap<Class<Fabric3Event>, List<Fabric3EventListener<Fabric3Event>>>();
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public void publish(Fabric3Event event) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Fabric3EventListener<Fabric3Event> listener : listeners) {
            listener.onEvent(event);
        }
    }

    @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
    public <T extends Fabric3Event> void subscribe(Class<T> type, Fabric3EventListener<T> listener) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(type);
        if (listeners == null) {
            listeners = new ArrayList<Fabric3EventListener<Fabric3Event>>();
            cache.put((Class<Fabric3Event>) type, listeners);
        }
        listeners.add((Fabric3EventListener<Fabric3Event>) listener);
    }

    @SuppressWarnings({"SuspiciousMethodCalls"})
    public <T extends Fabric3Event> void unsubscribe(Class<T> type, Fabric3EventListener<T> listener) {
        List<Fabric3EventListener<Fabric3Event>> listeners = cache.get(type);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }
}
