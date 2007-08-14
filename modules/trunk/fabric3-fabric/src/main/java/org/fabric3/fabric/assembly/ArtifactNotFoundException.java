package org.fabric3.fabric.assembly;

import org.fabric3.spi.assembly.ActivateException;

/**
 * @version $Rev$ $Date$
 */
public class ArtifactNotFoundException extends ActivateException {
    public ArtifactNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
