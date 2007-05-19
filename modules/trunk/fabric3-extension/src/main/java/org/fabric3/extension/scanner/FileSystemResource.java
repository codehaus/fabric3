package org.fabric3.extension.scanner;

import java.io.IOException;

/**
 * Represents a resource that is to be contributed to a domain.
 *
 * @version $Rev$ $Date$
 */
public interface FileSystemResource {

    String getName();

    boolean isChanged() throws IOException;

    public void reset() throws IOException;

}
