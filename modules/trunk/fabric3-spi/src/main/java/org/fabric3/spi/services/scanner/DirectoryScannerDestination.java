package org.fabric3.spi.services.scanner;

import java.net.URI;
import java.net.URL;

/**
 * Implementations are targets for file system resources and receive notifications of file system events.
 *
 * @version $Rev$ $Date$
 */
public interface DirectoryScannerDestination {

    /**
     * Notifies the destination of a resource added to the directory. Destinations must process the added resource in a
     * blocking fashion.
     *
     * @param location  the dereferenceable URL of the resource
     * @param checksum  the resource checksum
     * @param timestamp the artifact timestamp
     * @return a URI the destination uses to identify the resource
     * @throws DestinationException if an error occurs processing the resource
     */
    URI addResource(URL location, byte[] checksum, long timestamp) throws DestinationException;

    /**
     * Notifies the destination of a resource update. Destinations must process the update in a blocking fashion.
     *
     * @param artifactUri the URI used by the destination to identify the resource
     * @param location    the dereferenceable URL of the updated resource
     * @param checksum    the resource checksum
     * @param timestamp   the artifact timestamp
     * @throws DestinationException if an error occurs processing the resource
     */
    void updateResource(URI artifactUri, URL location, byte[] checksum, long timestamp) throws DestinationException;

    /**
     * Notifies the destination of a resource removal. Destinations must process the removal in a blocking fashion.
     *
     * @param artifactUri the URI used by the destination to identify the resource
     * @throws DestinationException if an error occurs processing the removal
     */
    void removeResource(URI artifactUri) throws DestinationException;

    /**
     * Returns true if a resource exists for the given URI.
     *
     * @param artifactUri the artifact URI
     * @return true if a resource exists for the given URI
     */
    boolean resourceExists(URI artifactUri);

    /**
     * Returns the ResourceMetaData for the artifact identified by the given URI.
     *
     * @param uri the artifact URI
     * @return the ResourceMetaData for the artifact identified by the given URI or null if the artifact does not exist
     */
    ResourceMetaData getResourceMetaData(URI uri);

}
