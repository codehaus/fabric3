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
