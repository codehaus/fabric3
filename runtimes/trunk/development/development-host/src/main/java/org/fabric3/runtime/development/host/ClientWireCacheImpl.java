package org.fabric3.runtime.development.host;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 */
public class ClientWireCacheImpl implements ClientWireCache {
    private Map<URI, Wire> cache = new ConcurrentHashMap<URI, Wire>();

    public Wire getWire(URI uri) {
        return cache.get(uri);
    }

    public void putWire(URI uri, Wire wire) {
        assert uri != null;
        cache.put(uri, wire);
    }

}
