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

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryAccessor;
import org.fabric3.binding.aq.destination.DestinationFactory;
import org.fabric3.binding.aq.host.AQHost;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.AQWireSourceDefinition;
import org.fabric3.binding.aq.monitor.AQMonitor;
import org.fabric3.binding.aq.transport.OneWayMessageListener;
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
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.Reference;

/**
 * AQ source wire attacher
 * 
 * @version $Revision: 3125 $ $Date: 2008-03-16 17:01:06 +0000 (Sun, 16 Mar 2008) $
 */
public class AQSourceWireAttacher implements SourceWireAttacher<AQWireSourceDefinition>, AQSourceWireAttacherMBean {

    private final Map<URI, WireSourceData> data;
    private final Map<URI, AQHost> hosts;
    private final AtomicBoolean start;    
    private ConnectionFactoryAccessor<XAQueueConnectionFactory> connectionFactoryAccessor;
    private DestinationFactory<XAQueueConnectionFactory> destinationFactory;
    private DataSourceRegistry dataSourceRegistry;
    private TransactionHandler transactionHandler;
    private AQHost aqHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private AQMonitor monitor;
    
    /**
     * Constructor
     */
    public AQSourceWireAttacher() {        
        data  = new ConcurrentHashMap<URI, WireSourceData>();
        hosts = new ConcurrentHashMap<URI, AQHost>();
        start = new AtomicBoolean();         
    }
    

    /**
     * Attaches the AQ binding
     */
    public void attachToSource(final AQWireSourceDefinition sourceDefinition, final PhysicalWireTargetDefinition targetDefinition, final Wire wire) throws WiringException {
        setInitialState(sourceDefinition.getMetadata());
        setWireSourceData(sourceDefinition, targetDefinition, wire);
        if (start.get()) {
            start(targetDefinition.getUri());
        }
    }

    /**
     * Unregister the AQ host
     */
    public void detachFromSource(final AQWireSourceDefinition sourceDefinition, final PhysicalWireTargetDefinition wireTargetDefinition,
            final Wire wire) throws WiringException {

    }

    /**
     * Not Supported
     */
    public void attachObjectFactory(AQWireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
    
    /**
     * Destroy
     */
    @Destroy
    public void destroy(){
        monitor.onSourceWire(" Cleaning ");
        data.clear();
        hosts.clear();
    }

    /**
     * start from the jmx console
     */
    public void start(final String serviceNamespace) {
       start.set(true);
       start(URI.create(serviceNamespace));   
    }

    /**
     * stop from jmx console
     */
    public void stop() {
        /* Get the operation names */
        for (Map.Entry<URI, AQHost> entry : hosts.entrySet()) {
            monitor.onSourceWire(" Unregister " + entry.getValue().getClass().getName() + " for " + entry.getKey());
        }        
    }

    /**
     * Gets the List of services
     */
    public List<String> getServiceNames() {
        final List<String> services = new ArrayList<String>();
        for (URI service : data.keySet()) {
            services.add(service.toASCIIString());
        }
        return services;
    }

    /**
     * Start the process for to listen on the queues
     */
    private void start(final URI serviceNamespace) {
        monitor.onSourceWire(" Attaching Source for " + serviceNamespace);
        final WireSourceData sourceData = getWireSourceData(serviceNamespace);
        
        final AQWireSourceDefinition sourceDefinition = sourceData.getSourceDefinition();
        final ClassLoader classloader = classLoaderRegistry.getClassLoader(sourceDefinition.getClassloaderURI());
        final Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops = getWireOpertaions(sourceData.getWire());
        final XAQueueConnectionFactory requestConnectionFactory = getFactory(sourceDefinition.getMetadata());
        final Destination reqDestination = getDestination(sourceDefinition.getMetadata(), requestConnectionFactory);
        final MessageListener listener = new OneWayMessageListener(ops);

        aqHost.registerListener(requestConnectionFactory, reqDestination, listener, transactionHandler, classloader, serviceNamespace);
        hosts.put(serviceNamespace, aqHost);
    }    

    /**
     * Injects the Factory for retrieving Connection Factories
     * 
     * @param connectionFactoryAccessor
     */
    @Reference
    protected void setConnectionFactoryAccessor(final ConnectionFactoryAccessor<XAQueueConnectionFactory> connectionFactoryAccessor) {
        this.connectionFactoryAccessor = connectionFactoryAccessor;
    }

    /**
     * Injects the transaction handler.
     * 
     * @param transactionHandler
     *            Transaction handler.
     */
    @Reference(required = true)
    protected void setTransactionHandler(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }

    /**
     * @param dataSource
     *            The dataSource to set.
     */
    @Reference(required = true)
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
    protected void setJmsHost(AQHost aqHost) {
        this.aqHost = aqHost;
    }

    /**
     * Injects the destination strategies.
     * 
     * @param strategies
     *            Destination strategies.
     */
    @Reference
    protected void setDestinationFactory(final DestinationFactory<XAQueueConnectionFactory> destinationFactory) {
        this.destinationFactory = destinationFactory;
    }

    /**
     * Injects the class loader registry.
     * 
     * @param classLoaderRegistry
     */
    @Reference
    protected void setClassloaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }

    /**
     * Injects the monitor
     * 
     * @param monitor
     */
    @Monitor
    protected void setMonitor(final AQMonitor monitor) {
        this.monitor = monitor;
    }

    /*
     * Gets the operational Methods for the service
     */
    private Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> getWireOpertaions(final Wire wire) {
        final Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> operations = new HashMap<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        /* Get the operation names */
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            operations.put(entry.getKey().getName(), entry);
        }
        return operations;
    }

    /*
     * Sets the initial state start or stopped
     */
    private void setInitialState(final AQBindingMetadata metadata) {
        final String state = (String) metadata.getDestination().getProperties().get("initialState");
        final ConsumeState consumeState = ConsumeState.valueOf(state);
        final boolean startState = consumeState == ConsumeState.start ? true : false;
        start.set(startState);
        monitor.onSourceWire("In " + consumeState.name() + " State ");
    }

    /*
     * Sets the wire source meta data
     */
    private void setWireSourceData(final AQWireSourceDefinition sourceDefinition, final PhysicalWireTargetDefinition targetDefinition, final Wire wire) {
        final WireSourceData wireData = new WireSourceData(sourceDefinition, targetDefinition, wire);
        data.put(targetDefinition.getUri(), wireData);
    }
    
    /**
     * Logic for getting the wire source data
     * @param serviceNamespace
     * @return
     */
    private WireSourceData getWireSourceData(final URI serviceNamespace) {
        final WireSourceData sourceData = data.get(serviceNamespace);
        if(sourceData == null){
            throw new IllegalArgumentException("The service name is not valid");
        }
        return sourceData;
    }

    /**
     * TODO MOVE INTO DIFFERENT CLASS
     */
    private Destination getDestination(AQBindingMetadata metadata, XAQueueConnectionFactory requestConnectionFactory) {
        DestinationDefinition destinationDefinition = metadata.getDestination();

        Destination reqDestination = destinationFactory.getDestination(destinationDefinition, requestConnectionFactory);
        return reqDestination;
    }

    /**
     * TODO MOVE INTO DIFFERENT CLASS
     */
    private XAQueueConnectionFactory getFactory(AQBindingMetadata metadata) {
        final String datasourceName = (String) metadata.getDestination().getProperties().get("datasource");
        metadata.setDataSource(dataSourceRegistry.getDataSource(datasourceName));

        XAQueueConnectionFactory requestConnectionFactory = connectionFactoryAccessor.getConnectionFactory(metadata);
        return requestConnectionFactory;
    }
}
