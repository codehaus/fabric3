/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.scanner.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.scanner.scanner.resource.FileResource;
import org.fabric3.spi.assembly.Assembly;
import org.fabric3.spi.services.event.EventService;
import org.fabric3.spi.services.event.Fabric3EventListener;
import org.fabric3.spi.scanner.FileSystemResource;
import org.fabric3.spi.scanner.FileSystemResourceFactory;
import org.fabric3.spi.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.scanner.ResourceMetaData;

/**
 * @version $Rev$ $Date$
 */
public class ContributionDirectoryScannerTestCase extends TestCase {
    public static final URI ARTIFACT_URI = URI.create("test");
    public static final URI ARTIFACT_URI2 = URI.create("test2");
    public static final URI ARTIFACT_URI3 = URI.create("test3");
    public static final QName DEPLOYABLE = new QName("deployable");
    private ContributionDirectoryScanner scanner;
    private ContributionService contributionService;
    private File directory;
    private FileSystemResourceFactoryRegistry registry;
    private MonitorFactory monitorFactory;
    private Assembly assembly;
    private EventService eventService;

    public void testContributeAndActivate() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.replay(contributionService);

        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    /**
     * Verifies a resource is not updated if no changes are detected
     */
    public void testNoChangeResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        // third run
        scanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    public void testUpdateResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.expect(contributionService.getContributionTimestamp(ARTIFACT_URI)).andReturn(0L);
        contributionService.update(EasyMock.isA(ContributionSource.class));
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        FileOutputStream stream = new FileOutputStream(artifact);
        stream.write("test".getBytes());
        stream.close();
        scanner.run();
        // second run
        scanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    public void testRemoveResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        ResourceMetaData metaData = EasyMock.createNiceMock(ResourceMetaData.class);
        EasyMock.expect(metaData.getChecksum()).andReturn(new byte[]{}).anyTimes();
        EasyMock.replay(metaData);

        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.expect(contributionService.exists(EasyMock.eq(ARTIFACT_URI))).andReturn(
                true);
        contributionService.remove(EasyMock.eq(ARTIFACT_URI));
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        artifact.delete();
        scanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    /**
     * Verifies a recovery scenario where a resource is added to the destination, the scanner becomes inactive, the
     * artifact is deleted from the scanner directory, and a new scanner is created. Recovery is initiated and the
     * destination should be notified of the removal.
     */
    public void testRemoveRecover() throws Exception {
        File artifact = new File(directory, "test.txt");
        ResourceMetaData metaData = EasyMock.createNiceMock(ResourceMetaData.class);
        EasyMock.expect(metaData.getChecksum()).andReturn(new byte[]{}).anyTimes();
        EasyMock.replay(metaData);

        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.expect(contributionService.exists(ARTIFACT_URI)).andReturn(true);
        contributionService.remove(EasyMock.eq(ARTIFACT_URI));
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy the artifact
        artifact.createNewFile();
        // simulate two runs since the first run will just cache entries
        scanner.run();
        scanner.run();
        artifact.delete();
        ContributionDirectoryScanner recoveredScanner =
                new ContributionDirectoryScanner(registry,
                                                 contributionService,
                                                 assembly,
                                                 eventService,
                                                 monitorFactory);
        recoveredScanner.init();
        recoveredScanner.recover();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    /**
     * Verifies a recovery scenario where a resource is added to the destination, the scanner becomes inactive, the
     * artifact is updated , and a new scanner is created. Recovery is initiated and the destination should be notified
     * of the update.
     */
    public void testUpdateRecover() throws Exception {
        File artifact = new File(directory, "test.txt");

        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.expect(contributionService.getContributionTimestamp(ARTIFACT_URI)).andReturn(0L);
        contributionService.update(EasyMock.isA(ContributionSource.class));
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // deploy the artifact
        artifact.createNewFile();
        // simulate two runs since the first run will just cache entries
        scanner.run();
        scanner.run();
        FileOutputStream stream = new FileOutputStream(artifact);
        stream.write("test".getBytes());
        stream.close();
        ContributionDirectoryScanner recoveredScanner =
                new ContributionDirectoryScanner(registry,
                                                 contributionService,
                                                 assembly,
                                                 eventService,
                                                 monitorFactory);
        recoveredScanner.init();
        recoveredScanner.recover();
        // initiate a second scan since the recover will cache the added file
        recoveredScanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    /**
     * Verifies a recovery scenario no resources exist, the scanner becomes inactive, the artifact created in the
     * directory, and a new scanner is created. Recovery is initiated and the destination should be notified of the
     * update.
     */
    public void testAddRecover() throws Exception {
        File artifact = new File(directory, "test.txt");

        EasyMock.expect(contributionService.contribute(EasyMock.eq("DefaultStore"),
                                                       EasyMock.isA(ContributionSource.class))).andReturn(ARTIFACT_URI);
        EasyMock.replay(contributionService);
        assembly.includeInDomain(DEPLOYABLE);
        EasyMock.replay(assembly);
        // simulate two runs since the first run will just cache entries
        scanner.run();
        scanner.run();
        // deploy the artifact
        artifact.createNewFile();
        ContributionDirectoryScanner recoveredScanner =
                new ContributionDirectoryScanner(registry,
                                                 contributionService,
                                                 assembly,
                                                 eventService,
                                                 monitorFactory);
        recoveredScanner.init();
        recoveredScanner.recover();
        // initiate a second scan since the recover will cache the added file
        recoveredScanner.run();
        EasyMock.verify(contributionService);
        EasyMock.verify(assembly);
    }

    protected void setUp() throws Exception {
        super.setUp();
        registry = new FileSystemResourceFactoryRegistryImpl();
        registry.register(new TestResourceFactory());
        ScannerMonitor monitor = EasyMock.createNiceMock(ScannerMonitor.class);
        EasyMock.replay(monitor);
        monitorFactory = EasyMock.createMock(MonitorFactory.class);
        EasyMock.expect(monitorFactory.getMonitor(ScannerMonitor.class)).andReturn(monitor).anyTimes();
        EasyMock.replay(monitorFactory);
        contributionService = EasyMock.createMock(ContributionService.class);
        List<Deployable> deployables = new ArrayList<Deployable>();
        deployables.add(new Deployable(DEPLOYABLE, Constants.COMPOSITE_TYPE));
        EasyMock.expect(contributionService.getDeployables(ARTIFACT_URI)).andReturn(deployables);

        assembly = EasyMock.createMock(Assembly.class);
        directory = new File("../deploy");
        clean();
        directory.mkdir();
        eventService = EasyMock.createMock(EventService.class);
        eventService.subscribe(EasyMock.isA(Class.class), EasyMock.isA(Fabric3EventListener.class));
        EasyMock.expectLastCall().anyTimes();
        EasyMock.replay(eventService);
        scanner = new ContributionDirectoryScanner(registry,
                                                   contributionService,
                                                   assembly,
                                                   eventService,
                                                   monitorFactory);
        scanner.init();
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        clean();
    }

    private void clean() {
        if (!directory.exists()) {
            return;
        }
        File file = new File(directory, "test.txt");
        if (file.exists()) {
            file.delete();
        }
        directory.delete();

    }

    private class TestResourceFactory implements FileSystemResourceFactory {

        public FileSystemResource createResource(File file) {
            if (!file.getName().endsWith(".txt")) {
                return null;
            }
            return new FileResource(file);
        }
    }
}
