/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.fabric3.cache.infinispan.runtime;

import org.fabric3.cache.spi.CacheRegistry;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStarted;
import org.infinispan.notifications.cachemanagerlistener.annotation.CacheStopped;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStartedEvent;
import org.infinispan.notifications.cachemanagerlistener.event.CacheStoppedEvent;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @version $Rev$ $Date$
 */
@Listener
public class InfinispanRegistry implements CacheRegistry {

    private ConcurrentMap<String, ConcurrentMap> caches = new ConcurrentHashMap<String, ConcurrentMap>();

    public ConcurrentMap getCache(String name) {
        return caches.get(name);
    }

    public Map<String, ConcurrentMap> getCaches() {
        return Collections.unmodifiableMap(caches);
    }

    public void register(String name, ConcurrentMap cache) {
        caches.put(name, cache);
    }

    public ConcurrentMap unregister(String name) {
        return caches.remove(name);
    }

    public void clear() {
        caches.clear();
    }

    @CacheStarted
    public void cacheStarted(CacheStartedEvent event) {
        String name = event.getCacheName();
        register(name, event.getCacheManager().getCache(name));
    }

    @CacheStopped
    public void cacheStopped(CacheStoppedEvent event) {
        unregister(event.getCacheName());
    }

}
