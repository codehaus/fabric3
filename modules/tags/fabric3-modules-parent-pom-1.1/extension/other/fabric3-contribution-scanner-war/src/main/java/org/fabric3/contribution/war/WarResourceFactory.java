/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.contribution.war;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scanner.spi.FileResource;
import org.fabric3.scanner.spi.FileSystemResource;
import org.fabric3.scanner.spi.FileSystemResourceFactory;
import org.fabric3.scanner.spi.FileSystemResourceFactoryRegistry;

/**
 * Creates a FileResource for SCA contribution jars
 *
 * @version $Rev: 1343 $ $Date: 2007-09-15 16:30:11 -0700 (Sat, 15 Sep 2007) $
 */
@EagerInit
public class WarResourceFactory implements FileSystemResourceFactory {

    public WarResourceFactory(@Reference FileSystemResourceFactoryRegistry registry) {
        registry.register(this);
    }

    public FileSystemResource createResource(File file) {
        if (!file.getName().endsWith(".war")) {
            return null;
        }
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file.getCanonicalPath());
            JarEntry entry = jarFile.getJarEntry("WEB-INF/sca-contribution.xml");
            if (entry == null) {
                return null;
            }
        } catch (FileNotFoundException e) {
            // no sca-contribution, ignore
            return null;
        } catch (IOException e) {
            throw new AssertionError(e);
        } finally {
            try {
                if (jarFile != null) {
                    jarFile.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return new FileResource(file);
    }
}