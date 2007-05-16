package org.fabric3.runtime.development;

import org.fabric3.host.Fabric3RuntimeException;

/**
 * Denotes an invalid runtime configuration or corrupt image
 *
 * @version $Rev$ $Date$
 */
public class InvalidConfigurationException extends Fabric3RuntimeException {

    public InvalidConfigurationException(String message) {
        super(message);
    }

    public InvalidConfigurationException(String message, String identifier) {
        super(message, identifier);
    }

    public InvalidConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
