package org.fabric3.fabric.services.contribution.processor;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;

/**
 * @version $Rev$ $Date$
 */
public class JarContributionProcessorTestCase extends TestCase {
    private LoaderRegistry loaderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private JarContributionProcessor processor;
    private URL location;

    public void testProcess() throws Exception {
        URI uri = URI.create("test");
        byte[] checksum = "test".getBytes();
        long timestamp = System.currentTimeMillis();
        Contribution contribution = new Contribution(uri, location, checksum, timestamp);
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        processor.processContent(contribution, uri, ccl.getResourceAsStream("./repository/test.jar"));
        EasyMock.verify(loaderRegistry);
        assertNotNull(contribution.getManifest());
    }


    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        location = ccl.getResource("./repository/test.jar");
        ContributionManifest manifest = new ContributionManifest();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        CompositeComponentType type1 = new CompositeComponentType(new QName("TestComposite1"));
        CompositeComponentType type2 = new CompositeComponentType(new QName("TestComposite1"));
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(ContributionManifest.class), EasyMock.isA(LoaderContext.class))).andReturn(manifest);
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(CompositeComponentType.class), EasyMock.isA(LoaderContext.class))).andReturn(type1);

        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(CompositeComponentType.class), EasyMock.isA(LoaderContext.class))).andReturn(type2);
        EasyMock.replay(loaderRegistry);
        ClassLoader cl = getClass().getClassLoader();
        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(cl);
        EasyMock.replay(classLoaderRegistry);
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", cl);
        List<URL> urls = new ArrayList<URL>();
        urls.add(location);
        ClasspathProcessorRegistry registry = EasyMock.createMock(ClasspathProcessorRegistry.class);
        EasyMock.expect(registry.process(EasyMock.isA(File.class))).andReturn(urls);
        EasyMock.replay(registry);
        processor = new JarContributionProcessor(loaderRegistry, classLoaderRegistry, xmlFactory, null, registry);
    }
}
