package org.fabric3.extension.scanner.resource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.scanner.FileSystemResource;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryResourceTestCase extends TestCase {

    /**
     * Tests tracking changes. Simulates an underlying file remaining unchanged for the first check and changing for the
     * second.
     *
     * @throws Exception
     */
    public void testChanges() throws Exception {
        DirectoryResource resource = new DirectoryResource("test");
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
