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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.lookup.destination.DestinationStrategy;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.JmsWireSourceDefinition;
import org.fabric3.binding.aq.model.physical.JmsWireTargetDefinition;
import org.fabric3.binding.aq.transport.Fabric3MessageListener;
import org.fabric3.binding.aq.transport.Fabric3MessageReceiver;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Wire attacher for JMS binding.
 * 
 * @version $Revision$ $Date: 2008-01-12 22:32:35 +0000 (Sat, 12 Jan
 *          2008) $
 */
@EagerInit
@Service(interfaces = { SourceWireAttacher.class, TargetWireAttacher.class })
public class AQWireAttacher implements SourceWireAttacher<JmsWireSourceDefinition>, TargetWireAttacher<JmsWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;

    private final TargetWireAttacherRegistry targetWireAttacherRegistry;

    // JMS host
    private AQHost jmsHost;

    // Number of listeners
    private int receiverCount = 1;

    /**
     * Destination strategies.
     */
    private Map<CreateOption, DestinationStrategy> destinationStrategies;

    /**
     * Connection factory strategies.
     */
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies;

    /**
     * Transaction handlers.
     */
    private TransactionHandler transactionHandler;

    /**
     * Classloader registry.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /** DataSource */
    private DataSourceRegistry dataSourceRegistry;

    /**
     * Injects the wire attacher registries.
     * 
     * @param sourceWireAttacherRegistry
     *            the registry for source wire attachers
     * @param targetWireAttacherRegistry
     *            the registry for target wire attachers
     */
    public AQWireAttacher(@Reference
    SourceWireAttacherRegistry sourceWireAttacherRegistry, @Reference
    TargetWireAttacherRegistry targetWireAttacherRegistry) {

        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;

    }

    @Init
    public void start() {
        sourceWireAttacherRegistry.register(JmsWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(JmsWireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(JmsWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(JmsWireTargetDefinition.class, this);
    }

    /**
     * Injects the destination strategies.
     * 
     * @param strategies
     *            Destination strategies.
     */
    @Reference
    public void setDestinationStrategies(Map<CreateOption, DestinationStrategy> strategies) {
        this.destinationStrategies = strategies;
    }

    /**
     * Injects the connection factory strategies.
     * 
     * @param strategies
     *            Connection factory strategies.
     */
    @Reference
    public void setConnectionFactoryStrategies(Map<CreateOption, ConnectionFactoryStrategy> strategies) {
        this.connectionFactoryStrategies = strategies;
    }

    /**
     * Injects the classloader registry.
     * 
     * @param classLoaderRegistry
     *            Classloader registry.
     */
    @Reference
    public void setClassloaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    /**
     * Injects the transaction handler.
     * 
     * @param transactionHandler
     *            Transaction handler.
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
     * @param jmsHost
     *            JMS Host to use.
     */
    @Reference(required = true)
    public void setJmsHost(AQHost jmsHost) {
        this.jmsHost = jmsHost;
    }

    /**
     * Configurable property for receiver count.
     * 
     * @param receiverCount
     *            Receiver count.
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
    public void attachToSource(JmsWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
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
        metadata.setDataSource(dataSourceRegistry.getDataSource("AQDataSource"));

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
        jmsHost.registerListener(reqDestination, reqCf, listeners, transactionType, transactionHandler, cl);

    }

    /**
     * @see org.fabric3.spi.builder.component.TargetWireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, JmsWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
        
        boolean syncResponse = false; 
        
      
        /* TODO REFCATOR BELOW */
        Destination resDestination = null;
        QueueConnectionFactory resCf = null;

        /* TODO REFCATOR BELOW */

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(targetDefinition.getClassloaderURI());

        AQBindingMetadata metadata = targetDefinition.getMetadata();
        metadata.setDataSource(dataSourceRegistry.getDataSource("AQDataSource"));

        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        QueueConnectionFactory reqCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        Destination reqDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, reqCf);

        /* TODO REFCATOR */
        destinationDefinition = metadata.getResponseDestination();
        
        if (destinationDefinition != null) {
            resCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

            resDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, resCf);
            syncResponse = true;
        }

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();

            Fabric3MessageReceiver messageReceiver = new Fabric3MessageReceiver(resDestination, resCf);
            Interceptor interceptor = new AQTargetInterceptor(op.getName(), reqDestination, reqCf, correlationScheme, messageReceiver, classLoader,syncResponse);

            chain.addInterceptor(interceptor);

        }

    }

    public void attachObjectFactory(JmsWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(JmsWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
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

import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.QueueConnectionFactory;
import javax.sql.DataSource;

import oracle.jdbc.pool.OracleDataSource;

import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.lookup.destination.DestinationStrategy;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.JmsWireSourceDefinition;
import org.fabric3.binding.aq.model.physical.JmsWireTargetDefinition;
import org.fabric3.binding.aq.transport.Fabric3MessageListener;
import org.fabric3.binding.aq.transport.Fabric3MessageReceiver;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Wire attacher for JMS binding.
 * 
 * @version $Revision$ $Date: 2008-01-12 22:32:35 +0000 (Sat, 12 Jan
 *          2008) $
 */
@EagerInit
@Service(interfaces = { SourceWireAttacher.class, TargetWireAttacher.class })
public class AQWireAttacher implements SourceWireAttacher<JmsWireSourceDefinition>, TargetWireAttacher<JmsWireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;

    private final TargetWireAttacherRegistry targetWireAttacherRegistry;

    // JMS host
    private AQHost jmsHost;

    // Number of listeners
    private int receiverCount = 1;

    /**
     * Destination strategies.
     */
    private Map<CreateOption, DestinationStrategy> destinationStrategies;

    /**
     * Connection factory strategies.
     */
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies;

    /**
     * Transaction handlers.
     */
    private TransactionHandler transactionHandler;

    /**
     * Classloader registry.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /** DataSource */
    private DataSourceRegistry dataSourceRegistry;

    /**
     * Injects the wire attacher registries.
     * 
     * @param sourceWireAttacherRegistry
     *            the registry for source wire attachers
     * @param targetWireAttacherRegistry
     *            the registry for target wire attachers
     */
    public AQWireAttacher(@Reference
    SourceWireAttacherRegistry sourceWireAttacherRegistry, @Reference
    TargetWireAttacherRegistry targetWireAttacherRegistry) {

        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;

    }

    @Init
    public void start() {
        sourceWireAttacherRegistry.register(JmsWireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(JmsWireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(JmsWireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(JmsWireTargetDefinition.class, this);
    }

    /**
     * Injects the destination strategies.
     * 
     * @param strategies
     *            Destination strategies.
     */
    @Reference
    public void setDestinationStrategies(Map<CreateOption, DestinationStrategy> strategies) {
        this.destinationStrategies = strategies;
    }

    /**
     * Injects the connection factory strategies.
     * 
     * @param strategies
     *            Connection factory strategies.
     */
    @Reference
    public void setConnectionFactoryStrategies(Map<CreateOption, ConnectionFactoryStrategy> strategies) {
        this.connectionFactoryStrategies = strategies;
    }

    /**
     * Injects the classloader registry.
     * 
     * @param classLoaderRegistry
     *            Classloader registry.
     */
    @Reference
    public void setClassloaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    /**
     * Injects the transaction handler.
     * 
     * @param transactionHandler
     *            Transaction handler.
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
     * @param jmsHost
     *            JMS Host to use.
     */
    @Reference(required = true)
    public void setJmsHost(AQHost jmsHost) {
        this.jmsHost = jmsHost;
    }

    /**
     * Configurable property for receiver count.
     * 
     * @param receiverCount
     *            Receiver count.
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
    public void attachToSource(JmsWireSourceDefinition sourceDefinition, PhysicalWireTargetDefinition targetDefinition, Wire wire)
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
        metadata.setDataSource(dataSourceRegistry.getDataSource("AQDataSource"));

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
        jmsHost.registerListener(reqDestination, reqCf, listeners, transactionType, transactionHandler, cl);

    }

    /**
     * @see org.fabric3.spi.builder.component.TargetWireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, JmsWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
        
        boolean syncResponse = false; 
        
      
        /* TODO REFCATOR BELOW */
        Destination resDestination = null;
        QueueConnectionFactory resCf = null;

        /* TODO REFCATOR BELOW */

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(targetDefinition.getClassloaderURI());

        AQBindingMetadata metadata = targetDefinition.getMetadata();
        metadata.setDataSource(dataSourceRegistry.getDataSource("AQDataSource"));

        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        QueueConnectionFactory reqCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        Destination reqDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, reqCf);

        /* TODO REFCATOR */
        destinationDefinition = metadata.getResponseDestination();
        
        if (destinationDefinition != null) {
            resCf = connectionFactoryStrategies.get(CreateOption.always).getConnectionFactory(metadata);

            resDestination = destinationStrategies.get(CreateOption.exists).getDestination(destinationDefinition, resCf);
            syncResponse = true;
        }

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();

            Fabric3MessageReceiver messageReceiver = new Fabric3MessageReceiver(resDestination, resCf);
            Interceptor interceptor = new AQTargetInterceptor(op.getName(), reqDestination, reqCf, correlationScheme, messageReceiver, classLoader,syncResponse);

            chain.addInterceptor(interceptor);

        }

    }

    public void attachObjectFactory(JmsWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(JmsWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
