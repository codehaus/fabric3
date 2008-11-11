package org.fabric3.management.contribution;

import java.net.URI;
import java.util.Set;

/**
 * @version $Revision$ $Date$
 */
public class ContributionInUseManagementException extends ContributionUninstallException {
    private static final long serialVersionUID = -4801764591014282993L;
    private Set<URI> contributions;
    private URI uri;

    /**
     * Constructor.
     *
     * @param message       the error message
     * @param uri           the contribution
     * @param contributions the installed contributions that reference the contribution
     */
    public ContributionInUseManagementException(String message, URI uri, Set<URI> contributions) {
        super(message);
        this.uri = uri;
        this.contributions = contributions;
    }

    public Set<URI> getContributions() {
        return contributions;
    }

    public URI getUri() {
        return uri;
    }
}
