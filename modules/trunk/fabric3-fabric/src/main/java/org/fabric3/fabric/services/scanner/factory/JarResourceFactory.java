package org.fabric3.fabric.services.scanner.factory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.services.scanner.resource.FileResource;
import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactory;
import org.fabric3.spi.services.scanner.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class JarResourceFactory implements FileSystemResourceFactory {

    public JarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().endsWith(".jar")) {
            return null;
        }
        JarURLConnection conn;
        try {
            ClassLoader cl = new URLClassLoader(new URL[] {file.toURL()});
            URL url = cl.getResource("/META-INF/sca-contribution.xml");
            conn = (JarURLConnection) url.openConnection();
            if (conn.getJarEntry() == null) {
                // not a contribution archive, ignore
                return null;
            }
        } catch (FileNotFoundException e) {
            // no sca-contribution, ignore
            return null;
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        return new FileResource(file);
    }
}
