package org.fabric3.runtime.development.host;

import java.net.URI;

import org.fabric3.spi.wire.Wire;

/**
 * Caches wires from client code to services created by {@link DevelopmentRuntimeImpl#connectTo(Class,String)}
 *
 * @version $Rev$ $Date$
 */
public interface ClientWireCache {

    /**
     * Returns a cached wire  or null.
     *
     * @param uri the target service URI
     * @return the cached wire or null
     */
    Wire getWire(URI uri);

    /**
     * Caches a wire.
     *
     * @param uri  the target service URI
     * @param wire the wire to cache
     */
    void putWire(URI uri, Wire wire);

}
