package org.fabric3.spi.services.scanner;

import java.io.IOException;

/**
 * Tracks changes to a file system resource.
 *
 * @version $Rev$ $Date$
 */
public interface FileSystemResource {

    String getName();

    boolean isChanged() throws IOException;

    byte[] getChecksum();

    public void reset() throws IOException;

}
