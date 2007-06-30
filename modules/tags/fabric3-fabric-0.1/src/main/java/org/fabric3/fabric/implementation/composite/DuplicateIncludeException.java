package org.fabric3.fabric.implementation.composite;

import org.fabric3.spi.loader.LoaderException;

/**
 * @version $Rev$ $Date$
 */
public class DuplicateIncludeException extends LoaderException {
    public DuplicateIncludeException(String message, String identifier) {
        super(message, identifier);
    }
}
