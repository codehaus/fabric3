package org.fabric3.spi.scanner;

import java.io.File;

/**
 * A registry for FileSystemResourceFactory instances
 *
 * @version $Rev$ $Date$
 */
public interface FileSystemResourceFactoryRegistry {

    /**
     * Registers the factory
     *
     * @param factory the factory to register
     */
    void register(FileSystemResourceFactory factory);

    /**
     * Creates a FileSystemResource for the given file by dispatching to a registered factory.
     *
     * @param file the file to create the FileSystemResource for
     * @return the FileSystemResource
     */
    FileSystemResource createResource(File file);
}
