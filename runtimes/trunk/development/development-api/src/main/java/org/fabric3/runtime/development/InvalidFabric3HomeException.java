package org.fabric3.runtime.development;

/**
 * @version $Rev$ $Date$
 */
public class InvalidFabric3HomeException extends InvalidConfigurationException {

    public InvalidFabric3HomeException(String message) {
        super(message);
    }

    public InvalidFabric3HomeException(String message, String identifier) {
        super(message, identifier);
    }
}
