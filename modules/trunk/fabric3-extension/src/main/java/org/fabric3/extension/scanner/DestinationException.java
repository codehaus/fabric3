package org.fabric3.extension.scanner;

import org.fabric3.host.Fabric3Exception;

/**
 * @version $Rev$ $Date$
 */
public class DestinationException extends Fabric3Exception {

    public DestinationException(String message, String identifier, Throwable cause) {
        super(message, identifier, cause);
    }

    public DestinationException(Throwable cause) {
        super(cause);
    }
}
