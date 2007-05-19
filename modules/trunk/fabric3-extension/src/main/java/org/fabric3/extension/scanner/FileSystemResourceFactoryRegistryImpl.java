package org.fabric3.extension.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the DeploymentResourceFactoryRegistry.
 *
 * @version $Rev$ $Date$
 */
public class FileSystemResourceFactoryRegistryImpl implements FileSystemResourceFactoryRegistry {
    private List<FileSystemResourceFactory> factories = new ArrayList<FileSystemResourceFactory>();

    public void register(FileSystemResourceFactory factory) {
        factories.add(factory);
    }

    public FileSystemResource createResource(File file) {
        for (FileSystemResourceFactory factory : factories) {
            FileSystemResource resource = factory.createResource(file);
            if (resource != null) {
                return resource;
            }
        }
        return null;
    }
}
