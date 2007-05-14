package org.fabric3.fabric.services.contribution.processor;

import java.net.URI;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.model.type.CompositeComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JarContributionProcessorTestCase extends TestCase {
    private LoaderRegistry loaderRegistry;
    private ClassLoaderRegistry classLoaderRegistry;
    private JarContributionProcessor processor;

    public void testProcess() throws Exception {
        URI uri = URI.create("test");
        Contribution contribution = new Contribution(uri);
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        contribution.setLocation(cl.getResource("./repository/test.jar"));
        processor.processContent(contribution, uri, cl.getResourceAsStream("./repository/test.jar"));
        EasyMock.verify(loaderRegistry);
        assertNotNull(contribution.getManifest());
    }


    protected void setUp() throws Exception {
        super.setUp();
        ContributionManifest manifest = new ContributionManifest();
        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        CompositeComponentType type1 = new CompositeComponentType(new QName("TestComposite1"));
        CompositeComponentType type2 = new CompositeComponentType(new QName("TestComposite1"));
        EasyMock.expect(loaderRegistry.load(EasyMock.isNull(),
                                      EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.isA(LoaderContext.class))).andReturn(manifest);
        EasyMock.expect(loaderRegistry.load(EasyMock.isNull(),
                                              EasyMock.isA(XMLStreamReader.class),
                                              EasyMock.isA(LoaderContext.class))).andReturn(type1);

        EasyMock.expect(loaderRegistry.load(EasyMock.isNull(),
                                              EasyMock.isA(XMLStreamReader.class),
                                              EasyMock.isA(LoaderContext.class))).andReturn(type2);
        EasyMock.replay(loaderRegistry);
        ClassLoader cl = getClass().getClassLoader();
        classLoaderRegistry = EasyMock.createMock(ClassLoaderRegistry.class);
        EasyMock.expect(classLoaderRegistry.getClassLoader(EasyMock.isA(URI.class))).andReturn(cl);
        EasyMock.replay(classLoaderRegistry);
        XMLInputFactory xmlFactory = XMLInputFactory.newInstance("javax.xml.stream.XMLInputFactory", cl);
        processor = new JarContributionProcessor(loaderRegistry, classLoaderRegistry, xmlFactory);
    }
}
