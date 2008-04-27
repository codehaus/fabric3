package org.fabric3.container.web.jetty;

import java.net.URL;

/**
 * Receives events related to web application activation and deactivation.
 *
 * @version $Revision$ $Date$
 */
public interface WebApplicationActivatorMonitor {

    /**
     * The web application has been activated
     *
     * @param url the WAR url
     */
    void activated(URL url);

    /**
     * The web application has been deactivated
     *
     * @param url the WAR url
     */
    void deactivated(URL url);
}
