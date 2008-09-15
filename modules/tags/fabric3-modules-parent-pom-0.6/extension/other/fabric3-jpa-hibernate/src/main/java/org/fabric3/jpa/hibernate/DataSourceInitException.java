package org.fabric3.jpa.hibernate;

import org.fabric3.jpa.spi.EmfBuilderException;

/**
 * @version $Revision$ $Date$
 */
public class DataSourceInitException extends EmfBuilderException {
    private static final long serialVersionUID = -5344376508087234040L;

    public DataSourceInitException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataSourceInitException(Throwable cause) {
        super(cause);
    }
}