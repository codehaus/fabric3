package org.fabric3.extension.scanner;

import java.io.File;

import junit.framework.TestCase;
import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class DeploymentResourceFactoryRegistryImplTestCase extends TestCase {

    public void testDispatch() {
        DeploymentResourceFactoryRegistryImpl registry = new DeploymentResourceFactoryRegistryImpl();
        DeploymentResourceFactory factory = EasyMock.createMock(DeploymentResourceFactory.class);
        DeploymentResource resource = EasyMock.createMock(DeploymentResource.class);
        EasyMock.expect(factory.createResource(EasyMock.isA(File.class))).andReturn(resource);
        EasyMock.replay(factory);
        registry.register(factory);
        assertNotNull(registry.createResource(new File("")));
        EasyMock.verify(factory);
    }
}
