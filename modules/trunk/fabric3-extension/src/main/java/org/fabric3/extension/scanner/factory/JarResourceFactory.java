package org.fabric3.extension.scanner.factory;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;

import org.fabric3.extension.scanner.DeploymentResource;
import org.fabric3.extension.scanner.DeploymentResourceFactory;
import org.fabric3.extension.scanner.resource.FileResource;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev$ $Date$
 */
public class JarResourceFactory implements DeploymentResourceFactory {

    public DeploymentResource createResource(File file) {
        if (!file.getName().endsWith(".jar")) {
            return null;
        }
        JarURLConnection conn;
        try {
            URL url = new URL("jar:" + file.getAbsolutePath() + "!/META-INF/sca-contribution.xml");
            conn = (JarURLConnection) url.openConnection();
            if (conn.getJarEntry() == null) {
                // not a contribution archive, ignore
                return null;
            }
        } catch (IOException e) {
            throw new AssertionError();
        }
        return new FileResource(file);
    }
}
