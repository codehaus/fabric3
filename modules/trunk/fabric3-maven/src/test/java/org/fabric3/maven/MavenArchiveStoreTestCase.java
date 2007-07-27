package org.fabric3.maven;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.spi.services.contribution.ContributionStoreRegistry;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class MavenArchiveStoreTestCase extends TestCase {

    public void testFind() throws Exception {
        ContributionStoreRegistry contributionStoreRegistry = EasyMock.createMock(ContributionStoreRegistry.class);
        contributionStoreRegistry.register(EasyMock.isA(MavenArchiveStore.class));
        EasyMock.replay(contributionStoreRegistry);
        MavenArchiveStore store = new MavenArchiveStore(contributionStoreRegistry);
        store.init();
        assertNotNull(store.find(URI.create("junit:junit:3.8.1")));
    }

}
