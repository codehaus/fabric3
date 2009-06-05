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
import java.util.Properties;

/**
 * Represents a parsed connection factory configuration from the runtime system configuration.
 *
 * @version $Revision$ $Date$
 */
public class ConnectionFactoryConfiguration {
    private ConnectionFactoryType type = ConnectionFactoryType.LOCAL;
    private String name;
    private URI brokerUri;
    private Properties properties = new Properties();

    public ConnectionFactoryType getType() {
        return type;
    }

    public void setType(ConnectionFactoryType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getBrokerUri() {
        return brokerUri;
    }

    public void setBrokerUri(URI brokerUri) {
        this.brokerUri = brokerUri;
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }

    public Properties getProperties() {
        return properties;
    }
}
