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

import java.util.Map;

import javax.jms.Destination;
import javax.jms.XAQueueConnectionFactory;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryAccessor;
import org.fabric3.binding.aq.destination.DestinationFactory;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.AQWireTargetDefinition;
import org.fabric3.binding.aq.monitor.AQMonitor;
import org.fabric3.binding.aq.tx.TransactionHandler;
import org.fabric3.binding.aq.wire.interceptor.OneWayAQTargetInterceptor;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.resource.DataSourceRegistry;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.Reference;

/**
 * Wire attacher for AQ binding.
 * @version $Revision$ $Date$
 */
public class AQTargetWireAttacher implements  TargetWireAttacher<AQWireTargetDefinition> {   
   
    /*Connection factory strategies. */
    private ConnectionFactoryAccessor<XAQueueConnectionFactory> connectionFactoryAccessor;
    
    /* Factory used for creating destinations */
    private DestinationFactory<XAQueueConnectionFactory> destinationFactory;
    
    /* Registry that holds class loaders */
    private ClassLoaderRegistry classLoaderRegistry;
    
    /* Registry that holds data sources */
    private DataSourceRegistry dataSourceRegistry;

    private TransactionHandler transactionHandler;

    private AQMonitor monitor;    
    

    /**
     * @see org.fabric3.spi.builder.component.TargetWireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(final PhysicalWireSourceDefinition sourceDefinition, final AQWireTargetDefinition targetDefinition, final Wire wire)  throws WiringException {
        monitor.onTargetWire(" Still Refactoring ");
        
        final ClassLoader classLoader = classLoaderRegistry.getClassLoader(targetDefinition.getClassloaderURI());               
     
        AQBindingMetadata metadata = targetDefinition.getMetadata();
        String datasourceName = (String)metadata.getDestination().getProperties().get("datasource");
        metadata.setDataSource(dataSourceRegistry.getDataSource(datasourceName));
        

        XAQueueConnectionFactory reqCf = connectionFactoryAccessor.getConnectionFactory(metadata);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        Destination reqDestination = destinationFactory.getDestination(destinationDefinition, reqCf);      
                
         
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();            
            Interceptor interceptor = new OneWayAQTargetInterceptor(op.getName(), reqCf, reqDestination, transactionHandler, classLoader);             
            chain.addInterceptor(interceptor);
        }
    }

    public ObjectFactory<?> createObjectFactory(AQWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }       
    
    /**
     * Injects the Factory for retrieving Connection Factories
     * @param connectionFactoryAccessor
     */
    @Reference
    protected void setConnectionFactoryAccessor(final ConnectionFactoryAccessor<XAQueueConnectionFactory> connectionFactoryAccessor){
        this.connectionFactoryAccessor = connectionFactoryAccessor;
    }
            
    /**
     * Injects the destination {@link DestinationFactory}
     * @param  destinationFactory
     */
    @Reference
    protected void setDestinationFactory(final DestinationFactory<XAQueueConnectionFactory> destinationFactory) {
        this.destinationFactory = destinationFactory;
    }
    
    /**
     * @param dataSource - the dataSource to set.
     */
    @Reference
    protected void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }
    
    /**
     * Injects the transaction handler.
     * @param transactionHandler Transaction handler.
     */
    @Reference(required = true)
    protected void setTransactionHandler(TransactionHandler transactionHandler) {
        this.transactionHandler = transactionHandler;
    }

    /**
     * Injects the class loader registry.
     * @param classLoaderRegistry
     */
    @Reference
    protected void setClassloaderRegistry(ClassLoaderRegistry classLoaderRegistry) {
        this.classLoaderRegistry = classLoaderRegistry;
    }
    
    /**
     * Injects the monitor
     * @param monitor
     */
    @Monitor
    protected void setMonitor(final AQMonitor monitor){
        this.monitor = monitor;
    }
}
