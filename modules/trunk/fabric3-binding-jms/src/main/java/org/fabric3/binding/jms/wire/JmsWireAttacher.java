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
package org.fabric3.binding.jms.wire;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageListener;

import org.fabric3.binding.jms.model.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.model.CorrelationScheme;
import org.fabric3.binding.jms.model.CreateOption;
import org.fabric3.binding.jms.model.DestinationDefinition;
import org.fabric3.binding.jms.model.JmsBindingMetadata;
import org.fabric3.binding.jms.model.physical.JmsWireSourceDefinition;
import org.fabric3.binding.jms.model.physical.JmsWireTargetDefinition;
import org.fabric3.binding.jms.transport.impl.StandaloneJmsHost;
import org.fabric3.binding.jms.wire.lookup.connectionfactory.AlwaysConnectionFactoryStrategy;
import org.fabric3.binding.jms.wire.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.wire.lookup.connectionfactory.IfNotExistConnectionFactoryStrategy;
import org.fabric3.binding.jms.wire.lookup.connectionfactory.NeverConnectionFactoryStrategy;
import org.fabric3.binding.jms.wire.lookup.destination.AlwaysDestinationStrategy;
import org.fabric3.binding.jms.wire.lookup.destination.DestinationStrategy;
import org.fabric3.binding.jms.wire.lookup.destination.IfNotExistDestinationStrategy;
import org.fabric3.binding.jms.wire.lookup.destination.NeverDestinationStrategy;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

/**
 * Wire attacher for JMS binding.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsWireAttacher implements WireAttacher<JmsWireSourceDefinition, JmsWireTargetDefinition> {

    /**
     * Number of receiver threads.
     */
    private int receiverThreads;
    
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
     * Injects the wire attacher registry and servlet host.
     * 
     * @param wireAttacherRegistry Wire attacher rehistry.
     * @param servletHost Servlet host.
     */
    public JmsWireAttacher(@Reference WireAttacherRegistry wireAttacherRegistry, 
                           @Property(name = "receiverThreads") int receiverThreads) {
        
        wireAttacherRegistry.register(JmsWireSourceDefinition.class, this);
        wireAttacherRegistry.register(JmsWireTargetDefinition.class, this);
        this.receiverThreads = receiverThreads;
        
        destinationStrategies.put(CreateOption.always, new AlwaysDestinationStrategy());
        destinationStrategies.put(CreateOption.never, new NeverDestinationStrategy());
        destinationStrategies.put(CreateOption.ifnotexist, new IfNotExistDestinationStrategy());
        
        connectionFactoryStrategies.put(CreateOption.always, new AlwaysConnectionFactoryStrategy());
        connectionFactoryStrategies.put(CreateOption.never, new NeverConnectionFactoryStrategy());
        connectionFactoryStrategies.put(CreateOption.ifnotexist, new IfNotExistConnectionFactoryStrategy());
        
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(JmsWireSourceDefinition sourceDefinition,
                               PhysicalWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        Map<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops =
            new HashMap<String, Map.Entry<PhysicalOperationDefinition, InvocationChain>>();

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            ops.put(entry.getKey().getName(), entry);
        }

        JmsBindingMetadata metadata = sourceDefinition.getMetadata();

        Hashtable<String, String> env = metadata.getEnv();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        CreateOption create = connectionFactoryDefinition.getCreate();        
        ConnectionFactory reqCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);
        
        connectionFactoryDefinition = metadata.getResponseConnectionFactory();
        create = connectionFactoryDefinition.getCreate();        
        ConnectionFactory resCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        create = destinationDefinition.getCreate();
        Destination reqDestination = destinationStrategies.get(create).getDestination(destinationDefinition, reqCf, env);

        destinationDefinition = metadata.getResponseDestination();
        create = destinationDefinition.getCreate();
        Destination resDestination = destinationStrategies.get(create).getDestination(destinationDefinition, resCf, env);
        
        MessageListener messageListener = new Fabric3MessageListener(resDestination, resCf, ops, correlationScheme, wire);
        StandaloneJmsHost serviceHandler = new StandaloneJmsHost(reqCf, reqDestination, receiverThreads, messageListener);
        
        serviceHandler.start();

    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition,
     *      org.fabric3.spi.model.physical.PhysicalWireTargetDefinition,
     *      org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition,
                               JmsWireTargetDefinition targetDefinition,
                               Wire wire) throws WiringException {

        JmsBindingMetadata metadata = targetDefinition.getMetadata();

        Hashtable<String, String> env = metadata.getEnv();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        CreateOption create = connectionFactoryDefinition.getCreate();        
        ConnectionFactory reqCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);
        
        connectionFactoryDefinition = metadata.getResponseConnectionFactory();
        create = connectionFactoryDefinition.getCreate();        
        ConnectionFactory resCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        create = destinationDefinition.getCreate();
        Destination reqDestination = destinationStrategies.get(create).getDestination(destinationDefinition, reqCf, env);

        destinationDefinition = metadata.getResponseDestination();
        create = destinationDefinition.getCreate();
        Destination resDestination = destinationStrategies.get(create).getDestination(destinationDefinition, resCf, env);

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            
            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();
            
            Fabric3MessageReceiver messageReceiver = new Fabric3MessageReceiver(resDestination, resCf);
            Interceptor interceptor = new JmsTargetInterceptor(op.getName(), reqDestination, reqCf, correlationScheme, messageReceiver);
            
            chain.addInterceptor(interceptor);
            
        }

    }

}
