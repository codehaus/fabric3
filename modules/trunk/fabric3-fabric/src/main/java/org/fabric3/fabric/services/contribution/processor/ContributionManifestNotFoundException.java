package org.fabric3.fabric.services.contribution.processor;

import org.fabric3.host.contribution.ContributionException;

/**
 * Dentes a missing SCA contribution manifest
 *
 * @version $Rev$ $Date$
 */
public class ContributionManifestNotFoundException extends ContributionException {

    public ContributionManifestNotFoundException(String identifier) {
        super("sca-contributution.xml not found", identifier);
    }
}
