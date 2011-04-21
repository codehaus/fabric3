package org.fabric3.assembly.exception;

/**
 * When name is not specified and it have to be.
 *
 * @author Michal Capo
 */
public class NameNotGivenException extends RuntimeException {

    public NameNotGivenException(String message) {
        super(message);
    }
}
