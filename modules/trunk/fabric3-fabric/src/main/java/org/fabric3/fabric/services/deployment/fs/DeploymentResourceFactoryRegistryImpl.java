package org.fabric3.fabric.services.deployment.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the DeploymentResourceFactoryRegistry.
 *
 * @version $Rev$ $Date$
 */
public class DeploymentResourceFactoryRegistryImpl implements DeploymentResourceFactoryRegistry {
    private List<DeploymentResourceFactory> factories = new ArrayList<DeploymentResourceFactory>();

    public void register(DeploymentResourceFactory factory) {
        factories.add(factory);
    }

    public DeploymentResource createResource(File file) {
        for (DeploymentResourceFactory factory : factories) {
            DeploymentResource resource = factory.createResource(file);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }
}
