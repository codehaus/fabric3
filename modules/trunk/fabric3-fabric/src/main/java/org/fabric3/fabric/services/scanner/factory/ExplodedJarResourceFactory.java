package org.fabric3.fabric.services.scanner.factory;

import java.io.File;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.scanner.resource.DirectoryResource;
import org.fabric3.fabric.services.scanner.resource.FileResource;
import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactory;
import org.fabric3.spi.services.scanner.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for exploded SCA contribution jars
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class ExplodedJarResourceFactory implements FileSystemResourceFactory {

    public ExplodedJarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.isDirectory()) {
            return null;
        }
        File manifest = new File(file, "/META-INF/sca-contribution.xml");
        if (!manifest.exists()) {
            // not a contribution archive, ignore
            return null;
        }
        DirectoryResource directoryResource = new DirectoryResource(file);
        // monitor everything in META-INF
        File metaInf = new File(file, "/META-INF");
        monitorResource(directoryResource, metaInf);
        return directoryResource;
    }

    private void monitorResource(DirectoryResource directoryResource, File file) {
        if (file.isDirectory()) {
            for (File entry : file.listFiles()) {
                if (entry.isFile()) {
                    directoryResource.addResource(new FileResource(entry));
                } else {
                    monitorResource(directoryResource, entry);
                }
            }
        } else {
            directoryResource.addResource(new FileResource(file));
        }

    }
}
