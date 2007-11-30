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
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.fabric.services.factories.xml.XMLFactoryImpl;
import org.fabric3.spi.loader.LoaderContext;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.services.contribution.ProcessorRegistry;
import org.fabric3.spi.services.contribution.Resource;
import org.fabric3.spi.services.contribution.ResourceProcessor;
import org.fabric3.spi.services.factories.xml.XMLFactory;

/**
 * @version $Rev$ $Date$
 */
public class XmlResourceProcessorTestCase extends TestCase {
    private static String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><start></start>";
    private XmlResourceProcessor processor;
    private ProcessorRegistry processorRegistry;
    private LoaderRegistry loaderRegistry;

    public void testDummy() {

    }

    public void _testDispatch() throws Exception {
        assertNotNull(processor.process(new ByteArrayInputStream(XML.getBytes())));
        // verify the resource processor registered
        EasyMock.verify(processorRegistry);
        // verify the XmlStreamReader was set to the first element, <start>
        EasyMock.verify(loaderRegistry);
    }

    protected void setUp() throws Exception {
        super.setUp();
        processorRegistry = EasyMock.createMock(ProcessorRegistry.class);
        processorRegistry.register(EasyMock.isA(ResourceProcessor.class));
        EasyMock.replay(processorRegistry);

        loaderRegistry = EasyMock.createMock(LoaderRegistry.class);
        EasyMock.expect(loaderRegistry.load(EasyMock.isA(XMLStreamReader.class),
                                            EasyMock.eq(Resource.class),
                                            EasyMock.isA(LoaderContext.class))).andAnswer(new IAnswer<Resource>() {
            public Resource answer() throws Throwable {
                XMLStreamReader reader = (XMLStreamReader) EasyMock.getCurrentArguments()[0];
                assertEquals("start", reader.getName().getLocalPart());
                return new Resource();
            }
        }
        );
        EasyMock.replay(loaderRegistry);
        XMLFactory factory = new XMLFactoryImpl();
        processor = new XmlResourceProcessor(processorRegistry, loaderRegistry, factory);
    }
}
