package org.fabric3.maven;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.maven.archive.MavenArchiveStore;

/**
 * @version $Rev$ $Date$
 */
public class MavenArchiveStoreTestCase extends TestCase {

    public void testFind() throws Exception {
        MavenArchiveStore store = new MavenArchiveStore();
        store.init();
        assertNotNull(store.find(URI.create("junit:junit:3.8.1")));
    }

}
