package org.fabric3.extension.scanner;

import java.net.URI;
import java.net.URL;

/**
 * @version $Rev$ $Date$
 */
public interface DirectoryScannerDestination {

    URI addResource(URL location, byte[] checksum) throws DestinationException;

    void updateResource(URI artifactUri, URL location, byte[] checksum) throws DestinationException;

    void removeResource(URI uri) throws DestinationException;

    boolean resourceExists(URI uri);

    byte[] getResourceChecksum(URI uri);

}
