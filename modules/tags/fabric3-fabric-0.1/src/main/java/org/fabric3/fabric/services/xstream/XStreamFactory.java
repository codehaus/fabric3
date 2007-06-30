package org.fabric3.fabric.services.xstream;

import com.thoughtworks.xstream.XStream;

/**
 * Implementations create <code>XStream</code> instances for serializing internal runtime data structures.
 *
 * @version $Rev$ $Date$
 */
public interface XStreamFactory {

    /**
     * Returns a new XStream instance.
     *
     * @return a new XStream instance
     */
    XStream createInstance();

}
