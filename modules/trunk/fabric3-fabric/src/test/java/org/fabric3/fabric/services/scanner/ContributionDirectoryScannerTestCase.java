package org.fabric3.fabric.services.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.assembly.DistributedAssembly;
import org.fabric3.fabric.services.scanner.resource.FileResource;
import org.fabric3.fabric.services.xstream.XStreamFactoryImpl;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.contribution.Constants;
import org.fabric3.host.contribution.ContributionService;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.Deployable;
import org.fabric3.host.monitor.MonitorFactory;
import org.fabric3.spi.services.scanner.FileSystemResource;
import org.fabric3.spi.services.scanner.FileSystemResourceFactory;
import org.fabric3.spi.services.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.spi.services.scanner.ResourceMetaData;

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
    private XStreamFactoryImpl xstreamFactory;
    private FileSystemResourceFactoryRegistry registry;
    private MonitorFactory monitorFactory;
    private DistributedAssembly assembly;

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
                                                 xstreamFactory,
                                                 null, monitorFactory);
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

        URL url = artifact.toURI().normalize().toURL();
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
                                                 xstreamFactory,
                                                 null, monitorFactory);
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

        URL url = artifact.toURI().normalize().toURL();
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
                                                 xstreamFactory,
                                                 null, monitorFactory);
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
        xstreamFactory = new XStreamFactoryImpl();
        ScannerMonitor monitor = EasyMock.createNiceMock(ScannerMonitor.class);
        EasyMock.replay(monitor);
        monitorFactory = EasyMock.createMock(MonitorFactory.class);
        EasyMock.expect(monitorFactory.getMonitor(ScannerMonitor.class)).andReturn(monitor).anyTimes();
        EasyMock.replay(monitorFactory);
        contributionService = EasyMock.createMock(ContributionService.class);
        List<Deployable> deployables = new ArrayList<Deployable>();
        deployables.add(new Deployable(DEPLOYABLE, Constants.COMPOSITE_TYPE));
        EasyMock.expect(contributionService.getDeployables(ARTIFACT_URI)).andReturn(deployables);

        assembly = EasyMock.createMock(DistributedAssembly.class);
        scanner = new ContributionDirectoryScanner(registry,
                                                   contributionService, assembly, xstreamFactory, null, monitorFactory);
        directory = new File("../deploy");
        directory.mkdir();
        FileHelper.cleanDirectory(directory);
    }


    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(directory);
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
