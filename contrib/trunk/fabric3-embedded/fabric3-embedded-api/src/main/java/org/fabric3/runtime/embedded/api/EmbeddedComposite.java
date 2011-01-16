package org.fabric3.runtime.embedded.api;

import org.fabric3.host.contribution.ContributionSource;

/**
 * Embedded composite used for embedded server.
 *
 * @author Michal Capo
 */
public interface EmbeddedComposite extends ContributionSource {

    /**
     * Content type of embedded composite.
     */
    public static final String CONTENT_TYPE = "application/vnd.fabric3.embedded";

}
