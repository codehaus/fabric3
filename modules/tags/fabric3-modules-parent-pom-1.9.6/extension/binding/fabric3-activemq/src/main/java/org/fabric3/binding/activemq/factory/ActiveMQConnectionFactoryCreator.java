/*
 * Fabric3
 * Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.binding.activemq.factory;

import java.net.URI;
import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQXAConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryCreationException;
import org.fabric3.binding.jms.spi.runtime.ConnectionFactoryType;
import org.fabric3.binding.jms.spi.runtime.ProviderConnectionFactoryCreator;
import org.fabric3.host.runtime.HostInfo;

/**
 * Creates JMS connection factories on demand.
 *
 * @version $Rev: 8717 $ $Date: 2010-03-15 15:02:02 +0100 (Mon, 15 Mar 2010) $
 */
@EagerInit
public class ActiveMQConnectionFactoryCreator implements ProviderConnectionFactoryCreator {
    private ConnectionFactoryTemplateRegistry registry;
    private URI brokerUri;

    public ActiveMQConnectionFactoryCreator(@Reference ConnectionFactoryTemplateRegistry registry, @Reference HostInfo info) {
        this.registry = registry;
        String brokerName = info.getRuntimeName().replace(":", ".");
        brokerUri = URI.create("vm://" + brokerName);
    }

    public ConnectionFactory create(String templateName) throws ConnectionFactoryCreationException {
        ConnectionFactoryConfiguration configuration = registry.getTemplate(templateName);
        if (configuration == null) {
            throw new ConnectionFactoryCreationException("Connection factory template not defined for: " + templateName);
        }
        ConnectionFactoryType type = configuration.getType();
        switch (type) {

        case XA:
            ActiveMQXAConnectionFactory xaFactory = new ActiveMQXAConnectionFactory(getUri(configuration));
            xaFactory.setProperties(configuration.getFactoryProperties());
            return xaFactory;
        default:
            // default to local pooled
            ActiveMQConnectionFactory wrapped= new ActiveMQConnectionFactory(getUri(configuration));
            wrapped.setProperties(configuration.getFactoryProperties());
            return new PooledConnectionFactory(wrapped);
        }
    }

    public void release(ConnectionFactory factory) {
        if (factory instanceof PooledConnectionFactory) {
            PooledConnectionFactory pooled = (PooledConnectionFactory) factory;
            pooled.stop();
        }

    }

    private URI getUri(ConnectionFactoryConfiguration configuration) {
        if (configuration.getBrokerUri() != null) {
            return configuration.getBrokerUri();
        }
        return brokerUri;
    }


}
