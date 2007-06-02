package org.fabric3.fabric.assembly;

/**
 * @version $Rev$ $Date$
 */
public class ArtifactNotFoundException extends ActivateException {
    public ArtifactNotFoundException(String message, String identifier) {
        super(message, identifier);
    }
}
