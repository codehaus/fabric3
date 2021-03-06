package org.fabric3.scanner.scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.scanner.FileSystemResourceFactory;
import org.fabric3.spi.scanner.FileSystemResource;

/**
 * Default implementation of the FileSystemResourceFactoryRegistry.
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
