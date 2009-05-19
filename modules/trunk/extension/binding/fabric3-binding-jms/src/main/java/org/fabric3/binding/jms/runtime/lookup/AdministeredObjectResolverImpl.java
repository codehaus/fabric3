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
package org.fabric3.binding.jms.runtime.lookup;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.runtime.JMSRuntimeMonitor;
import org.fabric3.binding.jms.runtime.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.destination.DestinationStrategy;

/**
 * @version $Revision$ $Date$
 */
public class AdministeredObjectResolverImpl implements AdministeredObjectResolver {
    private Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies = new HashMap<CreateOption, ConnectionFactoryStrategy>();
    private Map<CreateOption, DestinationStrategy> destinationStrategies = new HashMap<CreateOption, DestinationStrategy>();
    private JMSRuntimeMonitor monitor;
    private Map<String, ConnectionFactoryHolder> connectionCache = new HashMap<String, ConnectionFactoryHolder>();
    private Map<DestinationKey, Destination> destinationCache = new HashMap<DestinationKey, Destination>();

    public AdministeredObjectResolverImpl(@Reference Map<CreateOption, ConnectionFactoryStrategy> factoryStrategies,
                                          @Reference Map<CreateOption, DestinationStrategy> destinationStrategies,
                                          @Monitor JMSRuntimeMonitor monitor) {
        this.factoryStrategies = factoryStrategies;
        this.destinationStrategies = destinationStrategies;
        this.monitor = monitor;
    }

    @Destroy
    public void destroy() {
        for (ConnectionFactoryHolder holder : connectionCache.values()) {
            try {
                holder.getFactory().reset();
            } catch (JMSException e) {
                monitor.jmsListenerError(e);
            }
        }
        connectionCache.clear();
        destinationCache.clear();
    }


    public ConnectionFactory resolve(ConnectionFactoryDefinition definition, Hashtable<String, String> env)
            throws JmsLookupException {

        ConnectionFactory connectionFactory;
        String connectionFactoryName = definition.getName();
        CreateOption create = definition.getCreate();
        ConnectionFactoryHolder holder = connectionCache.get(connectionFactoryName);
        if (holder != null) {
            connectionFactory = holder.getFactory();
            holder.increment();
        } else {
            ConnectionFactory delegate = factoryStrategies.get(create).getConnectionFactory(definition, env);
            SharedConnectionFactory shared = new SharedConnectionFactory(delegate);
            connectionFactory = shared;
            holder = new ConnectionFactoryHolder(connectionFactoryName, shared);
            connectionCache.put(connectionFactoryName, holder);
        }
        return connectionFactory;
    }

    public void release(ConnectionFactoryDefinition definition) {
        String name = definition.getName();
        ConnectionFactoryHolder holder = connectionCache.get(name);
        if (holder != null && holder.getCount() == 1) {
            connectionCache.remove(name);
            holder.decrement();
        } else if (holder != null && holder.getCount() > 1) {
            holder.decrement();
        }
    }

    public Destination resolve(DestinationDefinition definition, ConnectionFactory cf, Hashtable<String, String> env) throws JmsLookupException {
        String name = definition.getName();
        DestinationKey key = new DestinationKey(cf.getClass().getName(), name);
        Destination destination = destinationCache.get(key);
        if (destination == null) {
            CreateOption create = definition.getCreate();
            destination = destinationStrategies.get(create).getDestination(definition, cf, env);
            destinationCache.put(key, destination);
        }
        return destination;
    }

    private class ConnectionFactoryHolder {
        private String name;
        private SharedConnectionFactory factory;
        // Maintains a count of the clients using the connection factory. When the count reaches 0, the underlying connection is closed.
        private int count;

        private ConnectionFactoryHolder(String name, SharedConnectionFactory factory) {
            this.name = name;
            this.factory = factory;
        }

        public String getName() {
            return name;
        }

        public SharedConnectionFactory getFactory() {
            return factory;
        }

        public int getCount() {
            return count;
        }

        public void increment() {
            ++count;
        }

        public void decrement() {
            --count;
        }
    }

    private class DestinationKey {
        private String connectionFactoryName;
        private String destinationName;

        public DestinationKey(String connectionFactoryName, String destinationName) {
            this.connectionFactoryName = connectionFactoryName;
            this.destinationName = destinationName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DestinationKey that = (DestinationKey) o;

            return !(connectionFactoryName != null ? !connectionFactoryName.equals(that.connectionFactoryName) : that.connectionFactoryName != null)
                    && !(destinationName != null ? !destinationName.equals(that.destinationName) : that.destinationName != null);

        }

        @Override
        public int hashCode() {
            int result = connectionFactoryName != null ? connectionFactoryName.hashCode() : 0;
            result = 31 * result + (destinationName != null ? destinationName.hashCode() : 0);
            return result;
        }
    }

}
