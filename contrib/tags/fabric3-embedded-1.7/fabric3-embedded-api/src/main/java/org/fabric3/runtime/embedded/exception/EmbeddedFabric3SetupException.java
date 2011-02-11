package org.fabric3.runtime.embedded.exception;

/**
 * @author Michal Capo
 */
public class EmbeddedFabric3SetupException extends RuntimeException {

    public EmbeddedFabric3SetupException(String message) {
        super(message);
    }

    public EmbeddedFabric3SetupException(String message, Throwable cause) {
        super(message, cause);
    }
}
