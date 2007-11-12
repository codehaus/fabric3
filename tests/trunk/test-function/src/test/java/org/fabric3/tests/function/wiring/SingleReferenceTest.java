package org.fabric3.tests.function.wiring;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.HelloService;

/**
 * @version $Rev$ $Date$
 */
public class SingleReferenceTest extends TestCase {

    private HelloService service;

    @Reference
    public void setTestService(HelloService service) {
        this.service = service;
    }

    /**
     * Tests reference promotion case where promotion URI only includes the component name and not the reference. SCA
     * allows defaulting if the component has only one reference.
     */
    public void testPromotedDefaultReference() throws Exception {
        assertNotNull(service);
    }

}
