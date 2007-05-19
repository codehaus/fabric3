package org.fabric3.extension.scanner;

import java.io.File;

/**
 * @version $Rev$ $Date$
 */
public interface FileSystemResourceFactoryRegistry {

    void register(FileSystemResourceFactory factory);

    FileSystemResource createResource(File file);
}
