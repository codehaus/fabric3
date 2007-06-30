package org.fabric3.fabric.services.contribution;

import java.io.File;
import java.net.URI;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.services.xstream.XStreamFactoryImpl;
import org.fabric3.fabric.util.FileHelper;
import org.fabric3.host.runtime.HostInfo;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ContributionStoreRegistry;
import org.fabric3.spi.services.contribution.QNameExport;
import org.fabric3.spi.services.contribution.QNameImport;

/**
 * @version $Rev$ $Date$
 */
public class MetaDataStoreImplTestCase extends TestCase {
    private static final String REPOSITORY = "target/repository/";
    private static final URI RESOURCE_URI = URI.create("DefaultStore/test-resource");
    private static final URI RESOURCE_URI2 = URI.create("DefaultStore/test-resource2");
    private static final QName IMPORT_EXPORT_QNAME = new QName("test", "test");
    private static final QName IMPORT_EXPORT_QNAME2 = new QName("test2", "test2");
    private MetaDataStoreImpl store;
    private HostInfo info;
    private ContributionStoreRegistry registry;

    public void testRecoverAndResolve() throws Exception {
        MetaDataStoreImpl store2 = new MetaDataStoreImpl(REPOSITORY, info, registry, new XStreamFactoryImpl());
        store2.init();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME);
        Contribution contribution = store2.resolve(imprt);
        assertEquals(RESOURCE_URI, contribution.getUri());
    }

    public void testResolve() throws Exception {
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME);
        Contribution contribution = store.resolve(imprt);
        assertEquals(RESOURCE_URI, contribution.getUri());
    }

    public void testTransitiveResolution() throws Exception {
        Contribution contribution = new Contribution(URI.create("resource"));
        ContributionManifest manifest = new ContributionManifest();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME2);
        manifest.addImport(imprt);
        contribution.setManifest(manifest);
        List<Contribution> contributions = store.resolveTransitiveImports(contribution);
        assertEquals(2, contributions.size());
    }

    protected void setUp() throws Exception {
        super.setUp();
        info = EasyMock.createMock(HostInfo.class);
        EasyMock.expect(info.getDomain()).andReturn(URI.create("fabric3://./domain/")).anyTimes();
        EasyMock.expect(info.getRuntimeId()).andReturn("runtime").anyTimes();
        EasyMock.replay(info);
        registry = EasyMock.createNiceMock(ContributionStoreRegistry.class);
        EasyMock.replay(registry);

        store = new MetaDataStoreImpl(REPOSITORY, info, registry, new XStreamFactoryImpl());
        store.init();
        Contribution contribution = new Contribution(RESOURCE_URI);
        ContributionManifest manifest = new ContributionManifest();
        QNameExport export = new QNameExport(IMPORT_EXPORT_QNAME);
        manifest.addExport(export);
        contribution.setManifest(manifest);
        store.store(contribution);

        Contribution contribution2 = new Contribution(RESOURCE_URI2);
        ContributionManifest manifest2 = new ContributionManifest();
        QNameImport imprt = new QNameImport(IMPORT_EXPORT_QNAME);
        manifest2.addImport(imprt);
        QNameExport export2 = new QNameExport(IMPORT_EXPORT_QNAME2);
        manifest2.addExport(export2);
        contribution2.setManifest(manifest2);
        store.store(contribution2);

    }

    protected void tearDown() throws Exception {
        super.tearDown();
        FileHelper.deleteDirectory(new File("target/repository"));
    }

}
