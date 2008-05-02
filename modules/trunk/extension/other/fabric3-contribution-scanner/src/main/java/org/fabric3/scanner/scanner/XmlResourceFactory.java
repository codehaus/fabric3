package org.fabric3.scanner.scanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.scanner.FileResource;
import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactory;
import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for XML-based contributions
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class XmlResourceFactory implements FileSystemResourceFactory {

    public XmlResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().toLowerCase().endsWith(".xml")) {
            return null;
        }
        return new FileResource(file);
    }
}