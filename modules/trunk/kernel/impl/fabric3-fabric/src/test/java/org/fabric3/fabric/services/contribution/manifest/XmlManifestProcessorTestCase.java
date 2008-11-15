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
package org.fabric3.fabric.services.contribution.manifest;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.services.xml.XMLFactoryImpl;
import org.fabric3.spi.services.contribution.ContributionManifest;
import org.fabric3.spi.services.contribution.XmlManifestProcessorRegistry;
import org.fabric3.spi.xml.XMLFactory;
import org.fabric3.scdl.ValidationContext;
import org.fabric3.scdl.DefaultValidationContext;

/**
 * @version $Rev$ $Date$
 */
public class XmlManifestProcessorTestCase extends TestCase {
    public static final QName QNAME = new QName("foo", "bar");
    public static final byte[] XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bar xmlns=\"foo\"/>".getBytes();
    public static final byte[] XML_DTD = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE bar>" +
            "<bar xmlns=\"foo\"/>").getBytes();

    private XmlManifestProcessor processor;
    private XmlManifestProcessorRegistry registry;

    public void testDispatch() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML);
        ContributionManifest manifest = new ContributionManifest();
        ValidationContext context = new DefaultValidationContext();
        processor.process(manifest, stream, context);
        EasyMock.verify(registry);
    }

    public void testDTDDispatch() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML_DTD);
        ContributionManifest manifest = new ContributionManifest();
        ValidationContext context = new DefaultValidationContext();
        processor.process(manifest, stream, context);
        EasyMock.verify(registry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        XMLFactory factory = new XMLFactoryImpl();
        registry = EasyMock.createMock(XmlManifestProcessorRegistry.class);
        registry.process(EasyMock.eq(QNAME),
                         EasyMock.isA(ContributionManifest.class),
                         EasyMock.isA(XMLStreamReader.class),
                         EasyMock.isA(ValidationContext.class));
        EasyMock.replay(registry);
        processor = new XmlManifestProcessor(null, registry, factory);


    }
}
