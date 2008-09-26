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
    private Map<Class<? extends Fabric3Event>, List<Fabric3EventListener>> cache;

    public EventServiceImpl() {
        cache = new ConcurrentHashMap<Class<? extends Fabric3Event>, List<Fabric3EventListener>>();
    }

    public <T extends Fabric3Event> void publish(T event) {
        List<Fabric3EventListener> listeners = cache.get(event.getClass());
        if (listeners == null) {
            return;
        }
        for (Fabric3EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public <T extends Fabric3Event> void subscribe(Class<T> type, Fabric3EventListener listener) {
        List<Fabric3EventListener> listeners = cache.get(type);
        if (listeners == null) {
            listeners = new ArrayList<Fabric3EventListener>();
            cache.put(type, listeners);
        }
        listeners.add(listener);
    }

    public <T extends Fabric3Event> void unsubscribe(Class<T> type, Fabric3EventListener listener) {
        List<Fabric3EventListener> listeners = cache.get(type);
        if (listeners == null) {
            return;
        }
        listeners.remove(listener);
    }
}
