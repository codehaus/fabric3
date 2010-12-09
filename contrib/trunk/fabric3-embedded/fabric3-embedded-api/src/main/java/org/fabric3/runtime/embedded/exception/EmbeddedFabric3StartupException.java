package org.fabric3.runtime.embedded.exception;

/**
 * @author Michal Capo
 */
public class EmbeddedFabric3StartupException extends Exception {

    public EmbeddedFabric3StartupException() {
    }

    public EmbeddedFabric3StartupException(String message) {
        super(message);
    }

    public EmbeddedFabric3StartupException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmbeddedFabric3StartupException(Throwable cause) {
        super(cause);
    }
}
