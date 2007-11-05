package org.fabric3.fabric.services.contribution.manifest;

import org.fabric3.spi.services.contribution.ManifestLoadException;

/**
 * @version $Rev$ $Date$
 */
public class InvalidManifestTypeException extends ManifestLoadException {

    public InvalidManifestTypeException(String message, String identifier) {
        super(message, identifier);
    }
}
