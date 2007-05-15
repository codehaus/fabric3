package org.fabric3.runtime.development.host;

import java.net.URL;

import org.fabric3.host.runtime.Fabric3Runtime;

/**
 * The contract between the Domain API and the development runtime implementation, which is loaded in a child
 * classloader of the application and Domain API classloader. This isolates runtime implementation classes from the
 * application classpath.
 *
 * @version $Rev$ $Date$
 */
public interface DevelopmentRuntime extends Fabric3Runtime<DevelopmentHostInfo> {

    /**
     * Activates a composite at the given URL.
     *
     * @param file the URL to the composite file
     */
    void activate(URL file);

    /**
     * Activates a composite from the given input stream.
     *
     * @param stream the composite input stream
     */
    //void activate(InputStream stream);

    /**
     * Stops the runtime instance/
     */
    void stop();
}
