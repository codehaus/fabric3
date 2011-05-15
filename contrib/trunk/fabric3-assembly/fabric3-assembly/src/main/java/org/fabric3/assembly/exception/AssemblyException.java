package org.fabric3.assembly.exception;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class AssemblyException extends RuntimeException {

    public AssemblyException(String message) {
        super(message);
    }

    public AssemblyException(String message, Object... pArgs) {
        super(MessageFormat.format(message, pArgs));
    }

    public AssemblyException(Throwable cause) {
        super(cause);
    }

    public AssemblyException(String message, Throwable cause) {
        super(message, cause);
    }
}
