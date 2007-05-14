package org.fabric3.fabric.implementation.processor;

import org.fabric3.spi.implementation.java.ProcessingException;

/**
 * @version $Rev$ $Date$
 */
public class UnknownScopeException extends ProcessingException {
    public UnknownScopeException(String message, String identifier) {
        super(message, identifier);
    }
}
