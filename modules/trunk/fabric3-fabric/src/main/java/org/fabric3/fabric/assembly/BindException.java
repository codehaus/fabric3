package org.fabric3.fabric.assembly;

/**
 * Denotes an error binding a service or reference
 *
 * @version $Rev$ $Date$
 */
public class BindException extends AssemblyException {

    public BindException(String message, String identifier) {
        super(message, identifier);
    }

    public BindException(Throwable cause) {
        super(cause);
    }

    public BindException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }
}
