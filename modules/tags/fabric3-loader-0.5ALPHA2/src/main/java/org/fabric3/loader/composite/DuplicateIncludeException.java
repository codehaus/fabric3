package org.fabric3.loader.composite;

import org.fabric3.introspection.xml.LoaderException;

/**
 * @version $Rev$ $Date$
 */
public class DuplicateIncludeException extends LoaderException {
    private static final long serialVersionUID = -3671246953971435103L;

    public DuplicateIncludeException(String message, String identifier) {
        super(message, identifier);
    }
}
