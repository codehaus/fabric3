/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.services.contribution;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.services.xmlfactory.XMLFactory;
import org.fabric3.services.xmlfactory.impl.XMLFactoryImpl;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionState;
import org.fabric3.spi.services.contribution.MetaDataStore;
import org.fabric3.spi.services.event.Recover;

/**
 * @version $Revision$ $Date$
 */
public class ContributionTrackingTestCase extends TestCase {
    private File repository;
    private ContributionTracker tracker;
    private ContributionReplayer replayer;
    private ContributionService contributionService;
    private MetaDataStore store;

    /**
     * Verifies contribution tracking and recovery. One mock contributions is installed and a second is stored.
     *
     * @throws Exception
     */
    public void testPersistAndReplay() throws Exception {
        URI uri = URI.create("test.jar");
        URL location = new File("test.jar").toURI().toURL();
        Contribution contribution1 = new Contribution(uri, location, new byte[0], -1, "application/java-archive", true);

        URI uri2 = URI.create("test2.jar");
        URL location2 = new File("test2.jar").toURI().toURL();
        Contribution contribution2 = new Contribution(uri2, location2, new byte[0], -1, "application/java-archive", true);

        tracker.onStore(contribution1);
        tracker.onStore(contribution2);

        contribution1.setState(ContributionState.INSTALLED);
        tracker.onInstall(contribution1);

        replayer.onEvent(new Recover());

        // verify the replayer stored both contributions and installed the first
        EasyMock.verify(store);
        EasyMock.verify(contributionService);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        XMLFactory factory = new XMLFactoryImpl();
        HostInfo info = EasyMock.createMock(HostInfo.class);
        File baseDir = new File(".");
        repository = new File(baseDir, "repository");
        repository.mkdir();

        EasyMock.expect(info.getBaseDir()).andReturn(baseDir).atLeastOnce();
        EasyMock.replay(info);

        // Setup and initialize the ContributionTracker
        ContributionTrackerMonitor trackerMonitor = EasyMock.createMock(ContributionTrackerMonitor.class);
        EasyMock.replay(trackerMonitor);
        tracker = new ContributionTracker(factory, info, trackerMonitor);
        tracker.init();

        // Setup the MetaDataStore. The store operation should be called twice
        store = EasyMock.createMock(MetaDataStore.class);
        store.store(EasyMock.isA(Contribution.class));
        EasyMock.expectLastCall().times(2);
        EasyMock.replay(store);

        // Setup the ContributionService. The install operation should be called once
        contributionService = EasyMock.createMock(ContributionService.class);
        contributionService.install(EasyMock.isA(List.class));
        EasyMock.replay(contributionService);

        // Setup the ContributionReplayer
        ContributionReplayMonitor replayMonitor = EasyMock.createMock(ContributionReplayMonitor.class);
        EasyMock.replay(replayMonitor);
        replayer = new ContributionReplayer(contributionService, store, factory, info, replayMonitor);

    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        new File(repository, "f3.xml").delete();
        repository.delete();
    }
}
