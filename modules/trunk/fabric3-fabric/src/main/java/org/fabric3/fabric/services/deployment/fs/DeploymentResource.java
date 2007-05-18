package org.fabric3.fabric.services.deployment.fs;

import java.io.IOException;

/**
 * Represents a resource that is to be contributed to a domain.
 *
 * @version $Rev$ $Date$
 */
public interface DeploymentResource {

    String getName();

    boolean isChanged() throws IOException;

    public void reset() throws IOException;

}
