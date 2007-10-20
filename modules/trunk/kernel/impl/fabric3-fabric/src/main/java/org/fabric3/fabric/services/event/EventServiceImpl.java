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
