package org.fabric3.host.contribution;

/**
 * @version $Rev$ $Date$
 */
public class ContributionNotFoundException extends ContributionException {
    /**
     * 
     */
    private static final long serialVersionUID = 6082773638859168837L;

    public ContributionNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
