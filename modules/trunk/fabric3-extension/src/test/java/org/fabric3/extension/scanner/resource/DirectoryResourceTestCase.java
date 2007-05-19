package org.fabric3.extension.scanner.resource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.scanner.FileSystemResource;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryResourceTestCase extends TestCase {

    public void testChanges() throws Exception {
        DirectoryResource resource = new DirectoryResource("test");
        FileSystemResource fileSystemResource = EasyMock.createMock(FileSystemResource.class);
        EasyMock.expect(fileSystemResource.isChanged()).andReturn(false);
        EasyMock.expect(fileSystemResource.isChanged()).andReturn(true);
        EasyMock.replay(fileSystemResource);
        resource.addResource(fileSystemResource);
        assertFalse(resource.isChanged());
        assertTrue(resource.isChanged());
        EasyMock.verify(fileSystemResource);
    }
}
