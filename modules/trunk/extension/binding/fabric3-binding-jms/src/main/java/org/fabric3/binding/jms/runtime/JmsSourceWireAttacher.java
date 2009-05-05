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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.destination.DestinationStrategy;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.serializer.SerializationException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.util.OperationTypeHelper;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.binding.serializer.Serializer;
import org.fabric3.spi.binding.serializer.SerializerFactory;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the target end of a wire (a service) to a JMS queue.
 *
 * @version $Revision$ $Date$
 */
public class JmsSourceWireAttacher implements SourceWireAttacher<JmsWireSourceDefinition>, JmsSourceWireAttacherMBean {
    private JmsHost jmsHost;
    private Map<CreateOption, DestinationStrategy> destinationStrategies = new HashMap<CreateOption, DestinationStrategy>();
    private Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies = new HashMap<CreateOption, ConnectionFactoryStrategy>();
    private ClassLoaderRegistry classLoaderRegistry;
    private TransactionHandler transactionHandler;
    private Map<String, SerializerFactory> serializerFactories = new HashMap<String, SerializerFactory>();

    public JmsSourceWireAttacher(@Reference TransactionHandler transactionHandler,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference JmsHost jmsHost,
                                 @Reference Map<CreateOption, DestinationStrategy> destinationStrategies,
                                 @Reference Map<CreateOption, ConnectionFactoryStrategy> connectionFactoryStrategies) {
        this.transactionHandler = transactionHandler;
        this.classLoaderRegistry = classLoaderRegistry;
        this.jmsHost = jmsHost;
        this.destinationStrategies = destinationStrategies;
        this.connectionFactoryStrategies = connectionFactoryStrategies;
    }

    @Reference(required = false)
    public void setSerializerFactories(Map<String, SerializerFactory> serializerFactories) {
        this.serializerFactories = serializerFactories;
    }

    public void attachToSource(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {

        JMSObjectFactory responseJMSObjectFactory = null;
        URI serviceUri = target.getUri();

        ClassLoader cl = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        JmsBindingMetadata metadata = source.getMetadata();
        Hashtable<String, String> env = metadata.getEnv();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();
        TransactionType transactionType = source.getTransactionType();

        ConnectionFactoryDefinition connectionFactory = metadata.getConnectionFactory();
        DestinationDefinition destination = metadata.getDestination();
        JMSObjectFactory requestJMSObjectFactory = buildObjectFactory(connectionFactory, destination, env);

        if (!metadata.noResponse()) {
            ConnectionFactoryDefinition responseConnectionFactory = metadata.getResponseConnectionFactory();
            DestinationDefinition responseDestination = metadata.getResponseDestination();
            responseJMSObjectFactory = buildObjectFactory(responseConnectionFactory, responseDestination, env);
        }

        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }

        Map<String, PayloadType> payloadTypes = source.getPayloadTypes();

        WireHolder wireHolder = createWireHolder(wire, payloadTypes, correlationScheme, transactionType, callbackUri, cl);

        SourceMessageListener messageListener;
        if (metadata.noResponse()) {
            messageListener = new OneWaySourceMessageListener(wireHolder);
        } else {
            messageListener = new RequestResponseSourceMessageListener(wireHolder);
        }
        if (jmsHost.isRegistered(serviceUri)) {
            // the wire has changed and it is being reprovisioned
            jmsHost.unregisterListener(serviceUri);
        }
        jmsHost.registerResponseListener(requestJMSObjectFactory,
                                         responseJMSObjectFactory,
                                         messageListener,
                                         transactionType,
                                         transactionHandler,
                                         cl,
                                         serviceUri);
    }

    public void detachFromSource(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        jmsHost.unregisterListener(target.getUri());
    }

    public void attachObjectFactory(JmsWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private WireHolder createWireHolder(Wire wire,
                                        Map<String, PayloadType> payloadTypes,
                                        CorrelationScheme correlationScheme,
                                        TransactionType transactionType,
                                        String callbackUri,
                                        ClassLoader cl) throws WiringException {
        List<InvocationChainHolder> chainHolders = new ArrayList<InvocationChainHolder>();
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            String dataBinding = definition.getDatabinding();
            Serializer inputSerializer = null;
            Serializer outputSerializer = null;
            if (dataBinding != null) {
                SerializerFactory factory = serializerFactories.get(dataBinding);
                if (factory == null) {
                    throw new WiringException("Serializer factory not found for: " + dataBinding);
                }
                Set<Class<?>> inputTypes = OperationTypeHelper.loadInParameterTypes(definition, cl);
                Set<Class<?>> faultTypes = OperationTypeHelper.loadFaultTypes(definition, cl);
                Set<Class<?>> outputTypes = OperationTypeHelper.loadOutputTypes(definition, cl);
                try {
                    inputSerializer = factory.getInstance(inputTypes, Collections.<Class<?>>emptySet());
                    outputSerializer = factory.getInstance(outputTypes, faultTypes);
                } catch (SerializationException e) {
                    throw new WiringException(e);
                }
            }
            PayloadType payloadType = payloadTypes.get(definition.getName());
            if (payloadType == null) {
                throw new WiringException("Payload type not found for operation: " + definition.getName());
            }
            chainHolders.add(new InvocationChainHolder(chain, inputSerializer, outputSerializer, payloadType));
        }
        return new WireHolder(chainHolders, callbackUri, correlationScheme, transactionType);
    }

    private JMSObjectFactory buildObjectFactory(ConnectionFactoryDefinition connectionFactoryDefinition,
                                                DestinationDefinition destinationDefinition,
                                                Hashtable<String, String> env) {

        CreateOption create = connectionFactoryDefinition.getCreate();
        ConnectionFactoryStrategy connectionStrategy = connectionFactoryStrategies.get(create);
        ConnectionFactory connectionFactory = connectionStrategy.getConnectionFactory(connectionFactoryDefinition, env);
        create = destinationDefinition.getCreate();
        DestinationStrategy destinationStrategy = destinationStrategies.get(create);
        Destination reqDestination = destinationStrategy.getDestination(destinationDefinition, connectionFactory, env);
        return new JMSObjectFactory(connectionFactory, reqDestination, 1);
    }

}
