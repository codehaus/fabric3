package org.fabric3.spi.services.scanner;

import java.io.File;

/**
 * Implementations create DeploymentResources for a given file
 *
 * @version $Rev$ $Date$
 */
public interface FileSystemResourceFactory {

    /**
     * Creates a deployment resource for the given file
     *
     * @param file the file to create the resource for
     * @return the deployment resource
     */
    FileSystemResource createResource(File file);

}
