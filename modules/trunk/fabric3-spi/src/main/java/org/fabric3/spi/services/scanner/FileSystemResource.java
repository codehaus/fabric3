package org.fabric3.spi.services.scanner;

import java.io.IOException;
import java.net.URL;

/**
 * Tracks changes to a file system resource.
 *
 * @version $Rev$ $Date$
 */
public interface FileSystemResource {

    String getName();

    URL getLocation();

    boolean isChanged() throws IOException;

    byte[] getChecksum();

    long getTimestamp();

    public void reset() throws IOException;

}
