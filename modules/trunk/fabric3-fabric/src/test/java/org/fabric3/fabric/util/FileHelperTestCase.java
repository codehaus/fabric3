package org.fabric3.fabric.util;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.fabric.util.FileHelper;

/**
 * @version $Rev$ $Date$
 */
public class FileHelperTestCase extends TestCase {

    public void testGetDomainPath() throws Exception {
        URI domain = URI.create("fabric3://./domain/");
        assertEquals("fabric3/domain", FileHelper.getDomainPath(domain));
    }
}
