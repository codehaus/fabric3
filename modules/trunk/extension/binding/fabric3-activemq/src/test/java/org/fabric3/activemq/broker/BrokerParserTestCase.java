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
package org.fabric3.activemq.broker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class BrokerParserTestCase extends TestCase {
    private static final String XML = "<foo><value><broker name='brokerName'>" +
            "    <networkConnectors>" +
            "        <networkConnector uri='multicast://default'/>" +
            "    </networkConnectors>" +
            "    <persistenceAdapter type='amq' syncOnWrite='false' maxFileLength='20 mb'/>" +
            "    <transportConnectors>" +
            "        <transportConnector name='openwire' uri='tcp://localhost:61616' discoveryUri='multicast://default'/>" +
            "        <transportConnector name='ssl' uri='ssl://localhost:61617'/>" +
            "        <transportConnector name='stomp' uri='stomp://localhost:61613'/>" +
            "        <transportConnector name='xmpp' uri='xmpp://localhost:61222'/>" +
            "    </transportConnectors>" +
            "</broker></value></foo>";
    private BrokerParser parser = new BrokerParser();

    public void testParse() throws Exception {
        InputStream stream = new ByteArrayInputStream(XML.getBytes());
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        reader.nextTag();
        BrokerConfiguration configuration = parser.parse(reader);
        assertEquals(1, configuration.getNetworkConnectorUris().size());
        assertEquals("multicast://default", configuration.getNetworkConnectorUris().get(0).toString());
        TransportConnectorConfig connectorConfig = configuration.getTransportConnectorConfigs().get(0);
        assertEquals("tcp://localhost:61616", connectorConfig.getUri().toString());
        assertEquals("multicast://default", connectorConfig.getDiscoveryUri().toString());
        assertEquals(4, configuration.getTransportConnectorConfigs().size());
    }

}
