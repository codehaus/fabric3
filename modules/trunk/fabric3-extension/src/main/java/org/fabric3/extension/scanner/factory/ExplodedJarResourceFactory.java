package org.fabric3.extension.scanner.factory;

import java.io.File;

import org.fabric3.extension.scanner.FileSystemResource;
import org.fabric3.extension.scanner.FileSystemResourceFactory;
import org.fabric3.extension.scanner.resource.DirectoryResource;
import org.fabric3.extension.scanner.resource.FileResource;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev$ $Date$
 */
public class ExplodedJarResourceFactory implements FileSystemResourceFactory {

    public FileSystemResource createResource(File file) {
        if (!file.isDirectory()) {
            return null;
        }
        File manifest = new File(file, "/META-INF/sca-contribution.xml");
        if (manifest.exists()) {
            // not a contribution archive, ignore
            return null;
        }
        DirectoryResource directoryResource = new DirectoryResource(file.getAbsolutePath());
        // monitor everything in META-INF
        File metaInf = new File(file, "/META-INF");
        for (File entry : metaInf.listFiles()) {
            directoryResource.addResource(new FileResource(entry));
        }
        return directoryResource;
    }
}
