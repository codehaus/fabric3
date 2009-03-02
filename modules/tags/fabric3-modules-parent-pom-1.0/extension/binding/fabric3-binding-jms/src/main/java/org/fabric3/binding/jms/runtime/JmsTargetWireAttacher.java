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
 * --- Original Apache License ---
 *
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
package org.fabric3.binding.jms.runtime;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.destination.DestinationStrategy;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the reference end of a wire to a JMS queue.
 * 
 * @version $Revision$ $Date$
 */
public class JmsTargetWireAttacher implements TargetWireAttacher<JmsWireTargetDefinition> {
    /**
     * Destination strategies.
     */
    private Map<CreateOption, DestinationStrategy> destinationStrategies = new HashMap<CreateOption, DestinationStrategy>();

    /**
     * Connection factory strategies.
     */
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies = new HashMap<CreateOption, ConnectionFactoryStrategy>();

    /**
     * Classloader registry.
     */
    private ClassLoaderRegistry classLoaderRegistry;

    /**
     * Injects the wire attacher registries.
     */
    public JmsTargetWireAttacher() {
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

    public void attachToTarget(PhysicalWireSourceDefinition sourceDefinition, JmsWireTargetDefinition targetDefinition, Wire wire)
            throws WiringException {
  
        JmsTargetMessageListener messageReceiver = null;
        Destination resDestination = null;
        ConnectionFactory resCf = null;
        
        ClassLoader cl = classLoaderRegistry.getClassLoader(targetDefinition.getClassLoaderId());

        JmsBindingMetadata metadata = targetDefinition.getMetadata();

        Hashtable<String, String> env = metadata.getEnv();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        CreateOption create = connectionFactoryDefinition.getCreate();

        ConnectionFactory reqCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);

        DestinationDefinition destinationDefinition = metadata.getDestination();
        create = destinationDefinition.getCreate();
        Destination reqDestination = destinationStrategies.get(create).getDestination(destinationDefinition, reqCf, env);

        if (!metadata.noResponse()) {
            connectionFactoryDefinition = metadata.getResponseConnectionFactory();
            create = connectionFactoryDefinition.getCreate();
            resCf = connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);

            destinationDefinition = metadata.getResponseDestination();
            create = destinationDefinition.getCreate();
            resDestination = destinationStrategies.get(create).getDestination(destinationDefinition, resCf, env);
        }

        Map<String, PayloadType> payloadTypes = targetDefinition.getPayloadTypes();
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

            PhysicalOperationDefinition op = entry.getKey();
            InvocationChain chain = entry.getValue();
            
            if(resDestination != null && resCf != null){
                messageReceiver = new JmsTargetMessageListener(resDestination, resCf);
            }
            String operationName = op.getName();
            PayloadType payloadType = payloadTypes.get(operationName);
            Interceptor interceptor = new JmsTargetInterceptor(operationName, payloadType, reqDestination, reqCf, correlationScheme, messageReceiver,
                    cl);

            chain.addInterceptor(interceptor);

        }

    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, JmsWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JmsWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }
}