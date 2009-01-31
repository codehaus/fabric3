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
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.host.JmsHost;
import org.fabric3.binding.jms.runtime.lookup.connectionfactory.ConnectionFactoryStrategy;
import org.fabric3.binding.jms.runtime.lookup.destination.DestinationStrategy;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
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
     * Injected JMS host.
     *
     * @param jmsHost JMS Host to use.
     */
    @Reference
    public void setJmsHost(JmsHost jmsHost) {
        this.jmsHost = jmsHost;
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

        Map<String, PayloadType> messageTypes = source.getPayloadTypes();
        Map<PhysicalOperationDefinition, InvocationChain> operations = wire.getInvocationChains();

        ResponseMessageListener messageListener;

        if (metadata.noResponse()) {
            messageListener = new OneWayMessageListenerImpl(operations, messageTypes, callbackUri);
        } else {
            messageListener = new ResponseMessageListenerImpl(operations, correlationScheme, messageTypes, transactionType, callbackUri);
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

    private JMSObjectFactory buildObjectFactory(ConnectionFactoryDefinition connectionFactoryDefinition,
                                                DestinationDefinition destinationDefinition,
                                                Hashtable<String, String> env) {
        CreateOption create = connectionFactoryDefinition.getCreate();

        ConnectionFactory connectionFactory =
                connectionFactoryStrategies.get(create).getConnectionFactory(connectionFactoryDefinition, env);
        create = destinationDefinition.getCreate();
        Destination reqDestination =
                destinationStrategies.get(create).getDestination(destinationDefinition, connectionFactory, env);
        return new JMSObjectFactory(connectionFactory, reqDestination, 1);
    }


}
