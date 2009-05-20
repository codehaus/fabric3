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
package org.fabric3.activemq.factory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;

import org.fabric3.binding.jms.runtime.factory.ConnectionFactoryRegistry;

/**
 * @version $Revision$ $Date$
 */
public class ConnectionFactoryParserTestCase extends TestCase {
    private static final String XML = "<foo><value>" +
            "<connection.factories>" +
            "   <connection.factory name='testFactory' broker.url='vm://broker' type='xa'>" +
            "      <optimizedMessageDispatch>true</optimizedMessageDispatch>" +
            "   </connection.factory>" +
            "   <connection.factory name='nonXAtestFactory' broker.url='vm://broker'/>" +
            "</connection.factories>" +
            "</value></foo>";

    private ConnectionFactoryParser parser;
    private XMLStreamReader reader;
    private MockConnectionFactoryRegistry registry;


    public void testParse() throws Exception {
        parser.setConnectionFactories(reader);
        parser.init();
        registry.verify();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        registry = new MockConnectionFactoryRegistry();
        parser = new ConnectionFactoryParser(registry);

        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();


    }

    private class MockConnectionFactoryRegistry implements ConnectionFactoryRegistry {
        private Map<String, ConnectionFactory> factories = new HashMap<String, ConnectionFactory>();

        public ConnectionFactory get(String name) {
            return factories.get(name);
        }

        public void register(String name, ConnectionFactory factory) {
            factories.put(name, factory);
        }

        public void unregister(String name) {
            factories.remove(name);
        }

        public void verify() {
            assertEquals(2, factories.size());
            ActiveMQXAConnectionFactory xaFactory = (ActiveMQXAConnectionFactory) factories.get("testFactory");
            assertEquals("vm://broker", xaFactory.getBrokerURL());
            ActiveMQConnectionFactory nonXaFactory = (ActiveMQConnectionFactory) factories.get("nonXAtestFactory");
            assertEquals("vm://broker", nonXaFactory.getBrokerURL());
        }
    }
}
