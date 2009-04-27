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
package org.fabric3.binding.net.loader;

import java.io.ByteArrayInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.binding.net.model.TcpBindingDefinition;
import org.fabric3.model.type.PolicyAware;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;

/**
 * @version $Revision$ $Date$
 */
public class TcpBindingLoaderTestCase extends TestCase {

    private static final String XML = "<binding.tcp readTimeout='10000' numberOfRetries='1' uri ='TestComponent'>" +
            "<wireFormat.testFormat/>" +
            "<response>" +
            "  <wireFormat.testResponseFormat/>" +
            "</response>" +
            "<sslSettings alias='sslSettings'/>" +
            "</binding.tcp>";


    public void testParse() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(new ByteArrayInputStream(XML.getBytes()));
        DefaultIntrospectionContext ctx = new DefaultIntrospectionContext();
        LoaderHelper helper = EasyMock.createMock(LoaderHelper.class);
        EasyMock.expect(helper.loadKey(reader)).andReturn(null);
        helper.loadPolicySetsAndIntents(EasyMock.isA(PolicyAware.class), EasyMock.eq(reader), EasyMock.eq(ctx));
        EasyMock.replay(helper);

        TcpBindingLoader loader = new TcpBindingLoader(helper);
        reader.nextTag();
        TcpBindingDefinition definition = loader.load(reader, ctx);

        // verify positioned correctly
        assertTrue(reader.isEndElement());
        assertEquals("binding.tcp", reader.getName().getLocalPart());

        // verify parsing
        assertEquals("testResponseFormat", definition.getConfig().getResponseWireFormat());
        assertEquals("sslSettings", definition.getConfig().getSslSettings());

    }

}