/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.scanner.impl;

import java.io.File;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scanner.impl.DirectoryResource;
import org.fabric3.scanner.spi.FileResource;
import org.fabric3.scanner.spi.FileSystemResource;
import org.fabric3.scanner.spi.FileSystemResourceFactory;
import org.fabric3.scanner.spi.FileSystemResourceFactoryRegistry;

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
