package org.fabric3.extension.scanner.resource;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.scanner.DeploymentResource;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryResourceTestCase extends TestCase {

    public void testChanges() throws Exception {
        DirectoryResource resource = new DirectoryResource("test");
        DeploymentResource deploymentResource = EasyMock.createMock(DeploymentResource.class);
        EasyMock.expect(deploymentResource.isChanged()).andReturn(false);
        EasyMock.expect(deploymentResource.isChanged()).andReturn(true);
        EasyMock.replay(deploymentResource);
        resource.addResource(deploymentResource);
        assertFalse(resource.isChanged());
        assertTrue(resource.isChanged());
        EasyMock.verify(deploymentResource);
    }
}
