package org.fabric3.loader.composite;

import org.fabric3.spi.loader.LoaderException;

/**
 * @version $Rev$ $Date$
 */
public class DuplicateIncludeException extends LoaderException {

    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -3671246953971435103L;

    public DuplicateIncludeException(String message, String identifier) {
        super(message, identifier);
    }
}
