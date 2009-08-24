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

package org.fabric3.jms.atomikos.host;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageListener;

import com.atomikos.jms.AtomikosConnectionFactoryBean;
import com.atomikos.jms.extra.MessageDrivenContainer;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.jms.spi.runtime.host.HostMonitor;
import org.fabric3.binding.jms.spi.runtime.host.JmsHost;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;

/**
 * JmsHost implementation that uses Atomikos transaction infrastructure to enable JMS MessageListeners to receive messages and dispatch them to a
 * service endpoint.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(JmsHost.class)
public class AtomikosJmsHost implements JmsHost, Fabric3EventListener<RuntimeStart> {
    private Map<URI, MessageDrivenContainer> containers = new ConcurrentHashMap<URI, MessageDrivenContainer>();
    private boolean started;
    private EventService eventService;
    private HostMonitor monitor;

    public AtomikosJmsHost(@Reference EventService eventService, @Monitor HostMonitor monitor) {
        this.eventService = eventService;
        this.monitor = monitor;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStart.class, this);
    }

    @Destroy
    public void destroy() {
        for (MessageDrivenContainer container : containers.values()) {
            container.stop();
        }
        started = false;
    }

    public void onEvent(RuntimeStart event) {
        // start receiving messages after the runtime has started
        for (Map.Entry<URI, MessageDrivenContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().start();
                monitor.registerListener(entry.getKey());
            } catch (JMSException e) {
                // TODO This should send an asynchronous notify
                monitor.error("Error starting service listener for " + entry.getKey(), e);
            }
        }
        started = true;
    }

    public boolean isRegistered(URI serviceUri) {
        return containers.containsKey(serviceUri);
    }

    public void register(URI serviceUri, MessageListener listener, Destination destination, ConnectionFactory factory) throws JMSException {
        if (!(factory instanceof AtomikosConnectionFactoryBean)) {
            // programming error
            throw new AssertionError("ConnectionFactory must be an instance of " + AtomikosConnectionFactoryBean.class.getName());
        }

        AtomikosConnectionFactoryBean bean = (AtomikosConnectionFactoryBean) factory;
        MessageDrivenContainer container = new MessageDrivenContainer();
        container.setDaemonThreads(true);
        container.setAtomikosConnectionFactoryBean(bean);
        container.setDestination(destination);
        container.setMessageListener(listener);
        //  FIXME add configuration
        //  container.setPoolSize();
        //  container.setExceptionListener();
        //  container.setTransactionTimeout();
        containers.put(serviceUri, container);
        if (started) {
            // the listener is provisioned after the runtime has started. start immediately
            // TODO add option for delayed start if the runtime is already started
            container.start();
            monitor.registerListener(serviceUri);
        }
    }

    public void unregister(URI serviceUri) {
        MessageDrivenContainer container = containers.remove(serviceUri);
        if (container != null) {
            container.stop();
            monitor.unRegisterListener(serviceUri);
        }
    }

}
