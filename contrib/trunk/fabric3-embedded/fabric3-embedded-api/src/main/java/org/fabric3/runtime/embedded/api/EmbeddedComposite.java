package org.fabric3.runtime.embedded.api;

import org.fabric3.host.contribution.ContributionSource;

/**
 * @author Michal Capo
 */
public interface EmbeddedComposite extends ContributionSource {

    public static final String CONTENT_TYPE = "application/vnd.fabric3.embedded";

    public static final String PREFIX = "embedded:";

}
