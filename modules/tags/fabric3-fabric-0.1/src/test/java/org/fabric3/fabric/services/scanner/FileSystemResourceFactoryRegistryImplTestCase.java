package org.fabric3.fabric.services.scanner;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactory;

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
