package org.fabric3.host.contribution;

/**
 * @version $Rev$ $Date$
 */
public class ContributionNotFoundException extends ContributionException {
    public ContributionNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
