/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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

package org.fabric3.binding.jms.runtime.container;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.transaction.TransactionManager;

import org.oasisopen.sca.annotation.Property;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.api.annotation.monitor.Monitor;
import org.fabric3.binding.jms.spi.common.TransactionType;
import org.fabric3.spi.event.EventService;
import org.fabric3.spi.event.Fabric3EventListener;
import org.fabric3.spi.event.RuntimeStart;
import org.fabric3.spi.management.ManagementException;
import org.fabric3.spi.management.ManagementService;
import org.fabric3.spi.transport.Transport;

import static org.fabric3.binding.jms.spi.runtime.JmsConstants.CACHE_CONNECTION;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
@Service(interfaces = {MessageContainerManager.class, Transport.class})
public class MessageContainerManagerImpl implements MessageContainerManager, Transport, Fabric3EventListener<RuntimeStart> {
    private static final int DEFAULT_TRX_TIMEOUT = 30;
    private Map<URI, AdaptiveMessageContainer> containers = new ConcurrentHashMap<URI, AdaptiveMessageContainer>();
    private boolean started;
    private EventService eventService;
    private ExecutorService executorService;
    private TransactionManager tm;
    private MessageContainerMonitor containerMonitor;
    private ManagementService managementService;
    private ContainerManagerMonitor managerMonitor;
    private int transactionTimeout = DEFAULT_TRX_TIMEOUT;

    public MessageContainerManagerImpl(@Reference EventService eventService,
                                       @Reference ExecutorService executorService,
                                       @Reference TransactionManager tm,
                                       @Reference ManagementService managementService,
                                       @Monitor MessageContainerMonitor containerMonitor,
                                       @Monitor ContainerManagerMonitor managerMonitor) {
        this.eventService = eventService;
        this.executorService = executorService;
        this.tm = tm;
        this.managementService = managementService;
        this.containerMonitor = containerMonitor;
        this.managerMonitor = managerMonitor;
    }

    @Property(required = false)
    public void setTransactionTimeout(int timeout) {
        if (timeout <= 0) {
            throw new IllegalArgumentException("Invalid transaction timeout: " + timeout);
        }
        this.transactionTimeout = timeout;
    }

    @Init
    public void init() {
        eventService.subscribe(RuntimeStart.class, this);
    }

    @Destroy
    public void destroy() throws JMSException {
        for (AdaptiveMessageContainer container : containers.values()) {
            container.stop();
        }
        for (AdaptiveMessageContainer container : containers.values()) {
            container.shutdown();
        }
        started = false;
    }

    public void suspend() {
        if (!started) {
            return;
        }
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().stop();
            } catch (JMSException e) {
                managerMonitor.stopError(entry.getKey(), e);
            }
        }
        started = false;
    }

    public void resume() {
        if (started) {
            return;
        }
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().start();
            } catch (JMSException e) {
                managerMonitor.startError(entry.getKey(), e);
            }
        }
        started = true;
    }

    public void onEvent(RuntimeStart event) {
        // start receiving messages after the runtime has started
        for (Map.Entry<URI, AdaptiveMessageContainer> entry : containers.entrySet()) {
            try {
                entry.getValue().initialize();
                managerMonitor.registerListener(entry.getKey());
            } catch (JMSException e) {
                managerMonitor.startError(entry.getKey(), e);
            }
        }
        started = true;
    }

    public boolean isRegistered(URI serviceUri) {
        return containers.containsKey(serviceUri);
    }

    public void register(ContainerConfiguration configuration) throws JMSException {
        ConnectionFactory factory = configuration.getFactory();
        TransactionType type = configuration.getType();
        URI uri = configuration.getUri();
        String clientId = configuration.getClientId();
        boolean durable = configuration.isDurable();
        int cacheLevel = configuration.getCacheLevel();
        boolean cacheConnection = cacheLevel >= CACHE_CONNECTION;

        // set the receive timeout to half of the trx timeout
        int receiveTimeout = transactionTimeout / 2;

        ContainerStatistics statistics = new ContainerStatistics();
        ConnectionManager connectionManager = new ConnectionManager(factory, uri, clientId, cacheConnection, durable, containerMonitor);
        UnitOfWork transactionHelper = new UnitOfWork(uri, type, transactionTimeout, tm, statistics);
        AdaptiveMessageContainer container = new AdaptiveMessageContainer(configuration,
                                                                          receiveTimeout,
                                                                          connectionManager,
                                                                          transactionHelper,
                                                                          statistics,
                                                                          executorService,
                                                                          containerMonitor);
        containers.put(uri, container);

        try {
            String encoded = encode(uri);
            managementService.export(uri.getFragment(), encoded, "JMS message container", container);
        } catch (ManagementException e) {
            throw new JMSException(e.getMessage());
        }
        if (started) {
            container.initialize();
            managerMonitor.registerListener(uri);
        }
    }

    public void unregister(URI uri) throws JMSException {
        AdaptiveMessageContainer container = containers.remove(uri);
        if (container != null) {
            container.shutdown();
            try {
                String encoded = encode(uri);
                managementService.export(uri.getFragment(), encoded, "JMS message container", container);
            } catch (ManagementException e) {
                throw new JMSException(e.getMessage());
            }
            managerMonitor.unRegisterListener(uri);
        }
    }

    private String encode(URI uri) {
        String path = uri.getPath();
        if (path.length() != 0) {
            return "JMS/message containers/" + path.substring(1);
        }
        return "JMS/message containers/" + uri.getAuthority();
    }


}
