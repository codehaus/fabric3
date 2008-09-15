package org.fabric3.container.web.jetty;

import java.net.URI;

/**
 * Receives events related to web application activation and deactivation.
 *
 * @version $Revision$ $Date$
 */
public interface WebApplicationActivatorMonitor {

    /**
     * The web application has been activated
     *
     * @param uri the WAR uri
     */
    void activated(URI uri);

    /**
     * The web application has been deactivated
     *
     * @param uri the WAR uri
     */
    void deactivated(URI uri);
}
