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
package org.fabric3.contribution.processor;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.contribution.MockXMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class XmlResourceProcessorTestCase extends TestCase {
    public static final QName QNAME = new QName("foo", "bar");
    public static final byte[] XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?><definitions xmlns=\"foo\"/>".getBytes();
    public static final byte[] XML_DTD = ("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<!DOCTYPE definitions>" +
            "<definitions xmlns=\"foo\"/>").getBytes();

    private XmlResourceProcessor processor;
//    private LoaderRegistry registry;

    public void testDispatch() throws Exception {
//        InputStream stream = new ByteArrayInputStream(XML);
//        processor.process(stream);
//        EasyMock.verify(registry);
    }

    public void testDTDDispatch() throws Exception {
//        InputStream stream = new ByteArrayInputStream(XML_DTD);
//        processor.process(stream);
//        EasyMock.verify(registry);
    }

    @SuppressWarnings({"unchecked"})
    protected void setUp() throws Exception {
        super.setUp();
//        registry = EasyMock.createMock(LoaderRegistry.class);
//        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
//                                      EasyMock.isA(Class.class),
//                                      EasyMock.isA(IntrospectionContext.class))).andReturn(null);
//        EasyMock.replay(registry);
        processor = new XmlResourceProcessor(null, null, null, new MockXMLFactory());


    }
}
