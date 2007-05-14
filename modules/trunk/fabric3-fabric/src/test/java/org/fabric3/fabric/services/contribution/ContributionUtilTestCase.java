package org.fabric3.fabric.services.contribution;

import java.net.URI;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ContributionUtilTestCase extends TestCase {

    public void testGetDomainPath() throws Exception {
        URI domain = URI.create("fabric3://./domain/");
        assertEquals("fabric3/domain", ContributionUtil.getDomainPath(domain));
    }
}
