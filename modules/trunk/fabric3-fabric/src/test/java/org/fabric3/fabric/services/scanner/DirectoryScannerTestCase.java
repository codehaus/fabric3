package org.fabric3.fabric.services.scanner;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.extension.scanner.DirectoryScannerDestination;
import org.fabric3.extension.scanner.FileSystemResource;
import org.fabric3.extension.scanner.FileSystemResourceFactory;
import org.fabric3.extension.scanner.FileSystemResourceFactoryRegistry;
import org.fabric3.extension.scanner.FileSystemResourceFactoryRegistryImpl;
import org.fabric3.fabric.services.scanner.resource.FileResource;
import org.fabric3.fabric.services.xstream.XStreamFactoryImpl;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * @version $Rev$ $Date$
 */
public class DirectoryScannerTestCase extends TestCase {
    public static final URI ARTIFACT_URI = URI.create("test");
    private DirectoryScanner scanner;
    private DirectoryScannerDestination destination;
    private File directory;

    public void testAddResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(destination.addResource(EasyMock.eq(artifact.toURI().toURL()),
                                                EasyMock.isA(byte[].class),
                                                EasyMock.anyLong())).andReturn(ARTIFACT_URI);
        EasyMock.replay(destination);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        EasyMock.verify(destination);
    }

    /**
     * Verifies a resource is not updated if no changes are detected
     */
    public void testNoChangeResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(destination.addResource(EasyMock.eq(artifact.toURI().toURL()),
                                                EasyMock.isA(byte[].class),
                                                EasyMock.anyLong())).andReturn(ARTIFACT_URI);
        EasyMock.replay(destination);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        // third run
        scanner.run();
        EasyMock.verify(destination);
    }

    public void testUpdateResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(destination.addResource(EasyMock.eq(artifact.toURI().toURL()),
                                                EasyMock.isA(byte[].class),
                                                EasyMock.anyLong())).andReturn(ARTIFACT_URI);
        EasyMock.expect(destination.getResourceTimestamp(EasyMock.eq(ARTIFACT_URI))).andReturn(0L);
        destination.updateResource(EasyMock.eq(ARTIFACT_URI),
                                   EasyMock.eq(artifact.toURI().toURL()),
                                   EasyMock.isA(byte[].class),
                                   EasyMock.anyLong());
        EasyMock.replay(destination);
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
        EasyMock.verify(destination);
    }

    public void testRemoveResource() throws Exception {
        File artifact = new File(directory, "test.txt");
        EasyMock.expect(destination.addResource(EasyMock.eq(artifact.toURI().toURL()),
                                                EasyMock.isA(byte[].class),
                                                EasyMock.anyLong())).andReturn(ARTIFACT_URI);
        EasyMock.expect(destination.getResourceChecksum(EasyMock.eq(ARTIFACT_URI))).andReturn(new byte[]{});
        destination.removeResource(EasyMock.eq(ARTIFACT_URI));
        EasyMock.replay(destination);
        // deploy a file
        artifact.createNewFile();
        // simulate first run
        scanner.run();
        // second run
        scanner.run();
        artifact.delete();
        scanner.run();
        EasyMock.verify(destination);
    }


    protected void setUp() throws Exception {
        super.setUp();
        FileSystemResourceFactoryRegistry registry = new FileSystemResourceFactoryRegistryImpl();
        registry.register(new TestResourceFactory());
        XStreamFactoryImpl xstreamFactory = new XStreamFactoryImpl();
        DirectoryScannerMonitor monitor = EasyMock.createMock(DirectoryScannerMonitor.class);
        EasyMock.replay(monitor);
        MonitorFactory monitorFactory = EasyMock.createMock(MonitorFactory.class);
        EasyMock.expect(monitorFactory.getMonitor(DirectoryScannerMonitor.class)).andReturn(monitor);
        EasyMock.replay(monitorFactory);
        destination = EasyMock.createMock(DirectoryScannerDestination.class);
        scanner = new DirectoryScanner(registry, destination, xstreamFactory, monitorFactory);
        directory = new File("deploy");
        directory.mkdir();
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
