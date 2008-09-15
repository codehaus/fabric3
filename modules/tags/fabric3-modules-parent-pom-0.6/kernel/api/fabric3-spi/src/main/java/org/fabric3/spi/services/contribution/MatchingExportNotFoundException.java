package org.fabric3.spi.services.contribution;

import org.fabric3.host.contribution.ContributionException;

/**
 * @version $Rev$ $Date$
 */
public class MatchingExportNotFoundException extends ContributionException {
    private static final long serialVersionUID = -7749644734169598789L;

    public MatchingExportNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
