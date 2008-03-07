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
package org.fabric3.loader.composite;

import java.net.URI;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamConstants;

import junit.framework.TestCase;

import org.fabric3.scdl.WireDefinition;
import org.fabric3.introspection.xml.LoaderException;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class WireLoaderTestCase extends TestCase {
    private WireLoader wireLoader;
    private XMLStreamReader reader;

    public void testWithNoServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source"), definition.getSource());
        assertEquals(URI.create("target"), definition.getTarget());
    }

    public void testWithServiceName() throws LoaderException, XMLStreamException {
        EasyMock.expect(reader.getAttributeValue(null, "source")).andReturn("source/s");
        EasyMock.expect(reader.getAttributeValue(null, "target")).andReturn("target/t");
        EasyMock.expect(reader.next()).andReturn(XMLStreamConstants.END_ELEMENT);
        EasyMock.replay(reader);
        WireDefinition definition = wireLoader.load(reader, null);
        EasyMock.verify(reader);
        assertEquals(URI.create("source#s"), definition.getSource());
        assertEquals(URI.create("target#t"), definition.getTarget());
    }

    protected void setUp() throws Exception {
        super.setUp();
        wireLoader = new WireLoader();
        reader = EasyMock.createStrictMock(XMLStreamReader.class);
    }
}
