package org.fabric3.fabric.services.deployment.fs;

import java.io.File;

/**
 * Implementations create DeploymentResources for a given file
 *
 * @version $Rev$ $Date$
 */
public interface DeploymentResourceFactory {

    /**
     * Creates a deployment resource for the given file
     *
     * @param file the file to create the resource for
     * @return the deployment resource
     */
    DeploymentResource createResource(File file);

}
