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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactory;

/**
 * @version $Rev$ $Date$
 */
public class FileSystemResourceFactoryRegistryImplTestCase extends TestCase {

    public void testDispatch() {
        FileSystemResourceFactoryRegistryImpl registry = new FileSystemResourceFactoryRegistryImpl();
        FileSystemResourceFactory factory = EasyMock.createMock(FileSystemResourceFactory.class);
        FileSystemResource resource = EasyMock.createMock(FileSystemResource.class);
        EasyMock.expect(factory.createResource(EasyMock.isA(File.class))).andReturn(resource);
        EasyMock.replay(factory);
        registry.register(factory);
        assertNotNull(registry.createResource(new File("")));
        EasyMock.verify(factory);
    }
}
