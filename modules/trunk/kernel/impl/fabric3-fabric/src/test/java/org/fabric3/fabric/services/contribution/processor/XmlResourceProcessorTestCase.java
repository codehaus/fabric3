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
package org.fabric3.fabric.services.contribution.processor;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.services.factories.xml.XMLFactoryImpl;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.factories.xml.XMLFactory;

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
    private LoaderRegistry registry;

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
        XMLFactory factory = new XMLFactoryImpl();
        registry = EasyMock.createMock(LoaderRegistry.class);
        EasyMock.expect(registry.load(EasyMock.isA(XMLStreamReader.class),
                                      EasyMock.isA(Class.class),
                                      EasyMock.isA(LoaderContext.class))).andReturn(null);
        EasyMock.replay(registry);
        processor = new XmlResourceProcessor(null, null, registry, factory);


    }
}
