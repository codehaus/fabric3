package org.fabric3.assembly.exception;

/**
 * When cannot find suitable server.
 *
 * @author Michal Capo
 */
public class ServerNotFoundException extends RuntimeException {

    public ServerNotFoundException(String message) {
        super(message);
    }
}
