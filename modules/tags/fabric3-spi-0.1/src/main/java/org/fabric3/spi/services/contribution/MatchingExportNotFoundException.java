package org.fabric3.spi.services.contribution;

import org.fabric3.host.contribution.ContributionException;

/**
 * @version $Rev$ $Date$
 */
public class MatchingExportNotFoundException extends ContributionException {
    public MatchingExportNotFoundException(String identifier) {
        super("No matching export found for", identifier);
    }
}
