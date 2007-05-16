package org.fabric3.runtime.development;

/**
 * Denotes an invalid Fabric3 home directory setting
 *
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
