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
import javax.jms.QueueConnectionFactory;

import org.fabric3.binding.aq.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.aq.lookup.destination.DestinationStrategy;
import org.fabric3.binding.aq.model.AQBindingMetadata;
import org.fabric3.binding.aq.model.CorrelationScheme;
import org.fabric3.binding.aq.model.CreateOption;
import org.fabric3.binding.aq.model.DestinationDefinition;
import org.fabric3.binding.aq.model.physical.AQWireTargetDefinition;
import org.fabric3.binding.aq.transport.Fabric3MessageReceiver;
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
 * Wire attacher for JMS binding.
 * 
 * @version $Revision$ $Date: 2008-01-12 22:32:35 +0000 (Sat, 12 Jan
 *          2008) $
 */
public class AQTargetWireAttacher implements  TargetWireAttacher<AQWireTargetDefinition> {   

    /**
     * Destination strategies.
     */
    private Map<CreateOption, DestinationStrategy> destinationStrategies;

    /**
     * Connection factory strategies.
     */
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies;   

    /**
     * Classloader registry.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /** DataSource */
    private DataSourceRegistry dataSourceRegistry;   

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
     * @param dataSource
     *            The dataSource to set.
     */
    @Reference
    protected void setDataSourceRegistry(DataSourceRegistry dataSourceRegistry) {
        this.dataSourceRegistry = dataSourceRegistry;
    }

    /**
     * @see org.fabric3.spi.builder.component.TargetWireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, AQWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
        
        boolean syncResponse = false; 
        
      
        /* TODO REFCATOR BELOW */
        Destination resDestination = null;
        QueueConnectionFactory resCf = null;

        /* TODO REFCATOR BELOW */

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(targetDefinition.getClassloaderURI());

        AQBindingMetadata metadata = targetDefinition.getMetadata();
        String datasourceName = (String)metadata.getDestination().getProperties().get("datasource");
        metadata.setDataSource(dataSourceRegistry.getDataSource(datasourceName));

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

    public ObjectFactory<?> createObjectFactory(AQWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
