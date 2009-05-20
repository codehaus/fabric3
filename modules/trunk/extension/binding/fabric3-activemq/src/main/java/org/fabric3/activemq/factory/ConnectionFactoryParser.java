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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.runtime.factory.ConnectionFactoryRegistry;

/**
 * Parses ConnectionFactoryConfiguration entries in the runtime system configuration, instantiates connection factories for them, and registers the
 * factories with the ConnectionFactoryRegistry.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class ConnectionFactoryParser {
    private List<ConnectionFactoryConfiguration> configurations = new ArrayList<ConnectionFactoryConfiguration>();
    private ConnectionFactoryRegistry registry;

    public ConnectionFactoryParser(@Reference ConnectionFactoryRegistry registry) {
        this.registry = registry;
    }

    @Property
    public void setConnectionFactories(XMLStreamReader reader) throws XMLStreamException, InvalidConfigurationException {
        reader.nextTag();
        ConnectionFactoryConfiguration configuration = null;
        while (true) {
            switch (reader.next()) {
            case XMLStreamConstants.START_ELEMENT:
                if ("connection.factory".equals(reader.getName().getLocalPart())) {
                    configuration = new ConnectionFactoryConfiguration();
                    String typeString = reader.getAttributeValue(null, "type");
                    if (typeString != null) {
                        ConnectionFactoryType type = ConnectionFactoryType.valueOf(typeString.trim().toUpperCase());
                        configuration.setType(type);
                    }
                    String name = reader.getAttributeValue(null, "name");
                    if (name == null) {
                        Location location = reader.getLocation();
                        int line = location.getLineNumber();
                        int col = location.getColumnNumber();
                        throw new InvalidConfigurationException("Connection factory name not configured [" + line + "," + col + "]");
                    }
                    configuration.setName(name);
                    String urlString = reader.getAttributeValue(null, "broker.url");
                    if (urlString == null) {
                        Location location = reader.getLocation();
                        int line = location.getLineNumber();
                        int col = location.getColumnNumber();
                        throw new InvalidConfigurationException("Broker URL not configured [" + line + "," + col + "]");
                    }
                    try {
                        URI uri = new URI(urlString);
                        configuration.setBrokerUri(uri);
                    } catch (URISyntaxException e) {
                        Location location = reader.getLocation();
                        int line = location.getLineNumber();
                        int col = location.getColumnNumber();
                        throw new InvalidConfigurationException("Invalid broker URL [" + line + "," + col + "]", e);
                    }
                } else {
                    if (configuration != null) {
                        // make sure the reader is in <connection.factory> and not before
                        configuration.setProperty(reader.getName().getLocalPart(), reader.getElementText());
                    }
                }

                break;
            case XMLStreamConstants.END_ELEMENT:
                if ("connection.factory".equals(reader.getName().getLocalPart())) {
                    configurations.add(configuration);
                    break;
                }
            case XMLStreamConstants.END_DOCUMENT:
                return;
            }


        }
    }


    @Init
    public void init() {
        // initialize and register the connection factories
        for (ConnectionFactoryConfiguration configuration : configurations) {
            URI uri = configuration.getBrokerUri();
            String name = configuration.getName();
            switch (configuration.getType()) {
            case LOCAL:
                ActiveMQConnectionFactory defaultFactory = new ActiveMQConnectionFactory(uri);
                defaultFactory.setProperties(configuration.getProperties());
                registry.register(name, defaultFactory);
                break;
            case POOLED:
                ActiveMQConnectionFactory wrapped = new ActiveMQConnectionFactory(uri);
                wrapped.setProperties(configuration.getProperties());
                PooledConnectionFactory pooledFactory = new PooledConnectionFactory(wrapped);
                registry.register(name, pooledFactory);
                // TODO set special pool properies
                break;
            case XA:
                ActiveMQXAConnectionFactory xaFactory = new ActiveMQXAConnectionFactory(uri);
                xaFactory.setProperties(configuration.getProperties());
                registry.register(name, xaFactory);
                break;
            }
        }
    }


    @Destroy
    public void destroy() {
        for (ConnectionFactoryConfiguration configuration : configurations) {
            registry.unregister(configuration.getName());
        }
    }

}
