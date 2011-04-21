package org.fabric3.assembly.exception;

/**
 * When configuration contains already a 'default' server or already contain server with such a name.
 *
 * @author Michal Capo
 */
public class ServerAlreadyExistsException extends RuntimeException {

    public ServerAlreadyExistsException(String message) {
        super(message);
    }
}
