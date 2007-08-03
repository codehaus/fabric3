package org.fabric3.fabric.services.contribution.processor;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.fabric.services.contenttype.ExtensionMapContentTypeResolver;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.services.contribution.ArtifactLocationEncoder;
import org.fabric3.spi.services.contribution.ClasspathProcessorRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.ProcessorRegistry;

/**
 * XCV TODO refactor this testcase as it does not test anything anymore
 *
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
        //ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        processor.processContent(contribution, uri);
        EasyMock.verify(loaderRegistry);
        assertNotNull(contribution.getManifest());
    }


    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        location = ccl.getResource("./repository/1/test.jar");
        ContributionManifest manifest = new ContributionManifest();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        EasyMock.expect(loaderRegistry.load(
                EasyMock.isA(XMLStreamReader.class),
                EasyMock.eq(ContributionManifest.class), EasyMock.isA(LoaderContext.class))).andReturn(manifest);
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

        ArtifactLocationEncoder encoder = EasyMock.createMock(ArtifactLocationEncoder.class);
        EasyMock.expect(encoder.encode(EasyMock.isA(URL.class))).andStubAnswer(new IAnswer<URL>() {
            public URL answer() throws Throwable {
                return (URL) EasyMock.getCurrentArguments()[0];
            }
        });
        EasyMock.replay(encoder);

        ExtensionMapContentTypeResolver contentTypeResolver = new ExtensionMapContentTypeResolver();
        ProcessorRegistry processorRegistry = EasyMock.createMock(ProcessorRegistry.class);
        EasyMock.expect(processorRegistry.processResource(EasyMock.isA(String.class),
                                                          EasyMock.isA(InputStream.class))).andReturn(null).atLeastOnce();
        EasyMock.replay(processorRegistry);
        processor = new JarContributionProcessor(processorRegistry,
                                                 loaderRegistry,
                                                 classLoaderRegistry,
                                                 xmlFactory,
                                                 null,
                                                 registry,
                                                 encoder,
                                                 contentTypeResolver);
    }
}
