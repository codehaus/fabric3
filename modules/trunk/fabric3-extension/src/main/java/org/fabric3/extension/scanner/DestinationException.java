package org.fabric3.extension.scanner;

import org.fabric3.host.Fabric3Exception;

/**
 * Indicates an error processing an event by a DirectoryScannerDestination
 *
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
