package org.fabric3.extension.scanner.resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.extension.scanner.DeploymentResource;

/**
 * Represents a directory that is to be contributed to a domain
 *
 * @version $Rev$ $Date$
 */
public class DirectoryResource implements DeploymentResource {
    private final String name;
    // the list of resources to track changes against
    private List<DeploymentResource> resources;

    public DirectoryResource(String name) {
        this.name = name;
        resources = new ArrayList<DeploymentResource>();
    }

    public String getName() {
        return name;
    }

    public boolean isChanged() throws IOException {
        for (DeploymentResource resource : resources) {
            if (resource.isChanged()) {
                return true;
            }
        }
        return false;
    }

    public void reset() throws IOException {
        for (DeploymentResource resource : resources) {
            resource.reset();
        }
    }

    public void addResource(DeploymentResource resource) {
        resources.add(resource);
    }
}
