package org.fabric3.extension.scanner;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

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
