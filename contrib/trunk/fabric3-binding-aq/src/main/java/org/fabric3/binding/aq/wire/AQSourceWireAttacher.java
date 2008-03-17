/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.binding.aq.wire;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;

import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.lookup.destination.DestinationStrategy;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.AQWireSourceDefinition;
import org.fabric3.binding.aq.transport.Fabric3MessageListener;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Wire attacher for JMS binding.
 *
 * @version $Revision: 3125 $ $Date: 2008-03-16 17:01:06 +0000 (Sun, 16 Mar 2008) $
 */
public class AQSourceWireAttacher implements SourceWireAttacher<AQWireSourceDefinition> {

    // JMS host
    private AQHost aqHost;

    // Number of listeners
    private int receiverCount = 1;

    /**
     * Destination strategies.
     */
    private Map<CreateOption, DestinationStrategy> destinationStrategies =
            new HashMap<CreateOption, DestinationStrategy>();

    /**
     * Connection factory strategies.
     */
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies =
            new HashMap<CreateOption, ConnectionFactoryStrategy>();

    /**
     * Transaction handlers.
     */
    private TransactionHandler transactionHandler;

    /**
     * Classloader registry.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * DataSource Registry
     */
    private DataSourceRegistry dataSourceRegistry;

    /**
     * Injects the wire attacher registries.
     *
     */
    public AQSourceWireAttacher() {
    }

    /**
     * Injects the destination strategies.
     *
     * @param strategies Destination strategies.
     */
    @Reference
    public void setDestinationStrategies(Map<CreateOption, DestinationStrategy> strategies) {
        this.destinationStrategies = strategies;
    }

    /**
     * Injects the connection factory strategies.
     *
     * @param strategies Connection factory strategies.
     */
    @Reference
    public void setConnectionFactoryStrategies(Map<CreateOption, ConnectionFactoryStrategy> strategies) {
        this.connectionFactoryStrategies = strategies;
    }

    /**
     * Injects the classloader registry.
     *
     * @param classLoaderRegistry Classloader registry.
     */
    @Reference
    public void setClassloaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    /**
     * Injects the transaction handler.
     *
     * @param transactionHandler Transaction handler.
     */
    @Reference
    public void setTransactionHandler(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }
    
    /**
     * @param dataSource
     *            The dataSource to set.
     */
    @Reference
    protected void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    /**
     * Injected JMS host.
     *
     * @param jmsHost JMS Host to use.
     */
    @Reference(required = true)
    public void setJmsHost(AQHost aqHost) {
        this.aqHost = aqHost;
    }

    /**
     * Configurable property for receiver count.
     *
     * @param receiverCount Receiver count.
     */
    @Property
    public void setReceiverCount(int receiverCount) {
        this.receiverCount = receiverCount;
    }

    /**
     * @see org.fabric3.spi.builder.component.SourceWireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(AQWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
        
        /** TODO REFCATOR BELOW */
        Destination resDestination = null;
        QueueConnectionFactory resCf = null;

        ClassLoader cl = classLoaderRegistry.getClassLoader(sourceDefinition.getClassloaderURI());

        Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops = new HashMap<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            ops.put(entry.getKey().getName(), entry);
        }

        AQBindingMetadata metadata = sourceDefinition.getMetadata();   
        String datasourceName = (String)metadata.getDestination().getProperties().get("datasource");
        metadata.setDataSource(dataSourceRegistry.getDataSource(datasourceName));

        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        QueueConnectionFactory reqCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

        DestinationDefinition destinationDefinition = metadata.getDestination();

        Destination reqDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, reqCf);

        /** TODO REFCATOR */
        destinationDefinition = metadata.getResponseDestination();
        
        if (destinationDefinition != null) {
            resCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

            resDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, resCf);
        }

        List<MessageListener> listeners = new LinkedList<MessageListener>();

        TransactionType transactionType = sourceDefinition.getTransactionType();

        /** TODO CREATE STRATGEIS FOR LISTNER */
        for (int i = 0; i < receiverCount; i++) {
            MessageListener listener = new Fabric3MessageListener(resDestination, resCf, ops, correlationScheme, transactionHandler,
                    transactionType);
            listeners.add(listener);
        }
        aqHost.registerListener(reqDestination, reqCf, listeners, transactionType, transactionHandler, cl);

    }

    /**
     * @see org.fabric3.spi.builder.component.SourceWireAttacher#attachObjectFactory(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, org.fabric3.spi.ObjectFactory)
     */
    public void attachObjectFactory(AQWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
