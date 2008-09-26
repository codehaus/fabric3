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
package org.fabric3.scanner.scanner.resource;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.scanner.scanner.DirectoryResource;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryResourceTestCase extends TestCase {

    /**
     * Tests tracking changes. Simulates an underlying file remaining unchanged for the first check and changing for the
     * second.
     */
    public void testChanges() throws Exception {
        DirectoryResource resource = new DirectoryResource(new File("test"));
        FileSystemResource fileSystemResource = EasyMock.createMock(FileSystemResource.class);
        fileSystemResource.reset();
        EasyMock.expect(fileSystemResource.getChecksum()).andReturn("test".getBytes());
        EasyMock.expect(fileSystemResource.getChecksum()).andReturn("test".getBytes());
        EasyMock.expect(fileSystemResource.getChecksum()).andReturn("test2".getBytes());
        EasyMock.replay(fileSystemResource);
        resource.addResource(fileSystemResource);
        resource.reset();
        assertFalse(resource.isChanged());
        assertTrue(resource.isChanged());
        EasyMock.verify(fileSystemResource);
    }
}
