package org.fabric3.assembly.exception;

/**
 * @author Michal Capo
 */
public class AssemblyException extends RuntimeException {

    public AssemblyException(String message) {
        super(message);
    }

    public AssemblyException(Throwable cause) {
        super(cause);
    }

    public AssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
