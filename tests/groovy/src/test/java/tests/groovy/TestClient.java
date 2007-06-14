package tests.groovy;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class TestClient extends TestCase {
    public @Reference EchoService service;

    public TestClient() {
        System.out.println("Hello");
    }

    public void testEcho() {
        assertEquals("Hello World", service.hello("World"));
    }
}
