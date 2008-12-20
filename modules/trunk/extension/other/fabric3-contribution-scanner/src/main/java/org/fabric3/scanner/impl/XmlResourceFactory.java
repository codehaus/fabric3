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

import org.fabric3.scanner.spi.FileResource;
import org.fabric3.scanner.spi.FileSystemResource;
import org.fabric3.scanner.spi.FileSystemResourceFactory;
import org.fabric3.scanner.spi.FileSystemResourceFactoryRegistry;

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