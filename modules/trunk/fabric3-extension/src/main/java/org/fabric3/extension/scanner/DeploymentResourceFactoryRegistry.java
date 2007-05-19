package org.fabric3.extension.scanner;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public interface DeploymentResourceFactoryRegistry {

    void register(DeploymentResourceFactory factory);

    DeploymentResource createResource(File file);
}
