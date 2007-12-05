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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.fabric.services.factories.xml.XMLFactoryImpl;
import org.fabric3.spi.services.contribution.Contribution;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.XmlProcessor;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class XmlContributionProcessorTestCase extends TestCase {
    private static String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><start></start>";
    private XmlContributionProcessor processor;
    private ProcessorRegistry processorRegistry;
    private XmlProcessor testProcessor;

    public void testDispatch() throws Exception {
        URL url = new URL("test", null, 0, "", new MockHandler());
        Contribution contribution = new Contribution(URI.create("test"), url, new byte[0], -1, null);
        processor.processContent(contribution, getClass().getClassLoader());
        // verify the contribution processor registered
        EasyMock.verify(processorRegistry);
        // verify the XmlStreamReader was set to the first element, <start>
        EasyMock.verify(testProcessor);
    }

    protected void setUp() throws Exception {
        super.setUp();
        processorRegistry = EasyMock.createMock(ProcessorRegistry.class);
        processorRegistry.register(EasyMock.isA(XmlContributionProcessor.class));
        EasyMock.replay(processorRegistry);

        XMLFactory factory = new XMLFactoryImpl();

        processor = new XmlContributionProcessor(processorRegistry, null, factory);

        testProcessor = EasyMock.createMock(XmlProcessor.class);
        EasyMock.expect(testProcessor.getType()).andReturn(new QName(null, "start"));
        testProcessor.processContent(EasyMock.isA(Contribution.class), EasyMock.isA(XMLStreamReader.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                XMLStreamReader reader = (XMLStreamReader) EasyMock.getCurrentArguments()[1];
                assertEquals("start", reader.getName().getLocalPart());
                return null;
            }
        });
        EasyMock.replay(testProcessor);
        processor.register(testProcessor);
    }

    private static class MockHandler extends URLStreamHandler {
        protected URLConnection openConnection(URL url) throws IOException {
            return new MockConnection(url);
        }
    }

    private static class MockConnection extends URLConnection {
        protected MockConnection(URL url) {
            super(url);
        }

        public void connect() throws IOException {

        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(XML.getBytes());
        }
    }
}
