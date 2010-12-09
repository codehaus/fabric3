package org.fabric3.runtime.embedded.api;

import org.fabric3.host.contribution.ContributionSource;

/**
 * @author Michal Capo
 */
public interface EmbeddedComposite extends ContributionSource {

    public static final String CONTENT_TYPE_CLASSPATH = "application/vnd.fabric3.embedded-classpath";
    public static final String CONTENT_TYPE_FILE = "application/vnd.fabric3.embedded-file";

    public static final String EMBEDDED = "embedded.";
    public static final String EMBEDDED_CLASSPATH = "embedded.classpath:";
    public static final String EMBEDDED_FILE = "embedded.file:";

}
