/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
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
