package org.fabric3.maven.archive;

import java.net.URI;

import junit.framework.TestCase;

/**
 * @version $Rev: 5976 $ $Date: 2008-11-16 16:10:37 -0800 (Sun, 16 Nov 2008) $
 */
public class MavenRepositoryTestCase extends TestCase {

    public void testFind() throws Exception {
        MavenRepository repository = new MavenRepository();
        repository.init();
        assertNotNull(repository.find(URI.create("junit:junit:3.8.1")));
    }

}
