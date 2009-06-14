/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.ConnectionFactoryDefinition;
import org.fabric3.binding.jms.common.CorrelationScheme;
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.lookup.AdministeredObjectResolver;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;
import org.fabric3.binding.jms.runtime.tx.TransactionHandler;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
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
    private TransactionHandler transactionHandler;
    private JmsHost jmsHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private AdministeredObjectResolver resolver;
    private Map<String, ParameterEncoderFactory> parameterEncoderFactories = new HashMap<String, ParameterEncoderFactory>();
    private Map<String, MessageEncoder> messageFormatters = new HashMap<String, MessageEncoder>();

    public JmsSourceWireAttacher(@Reference AdministeredObjectResolver resolver,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference TransactionHandler transactionHandler,
                                 @Reference JmsHost jmsHost) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
        this.transactionHandler = transactionHandler;
        this.jmsHost = jmsHost;
    }

    @Reference
    public void setParameterEncoderFactories(Map<String, ParameterEncoderFactory> parameterEncoderFactories) {
        this.parameterEncoderFactories = parameterEncoderFactories;
    }

    @Reference
    public void setMessageFormatters(Map<String, MessageEncoder> messageFormatters) {
        this.messageFormatters = messageFormatters;
    }

    public void attachToSource(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        ServiceListenerConfiguration configuration = new ServiceListenerConfiguration();
        configuration.setServiceUri(target.getUri());
        ClassLoader classloader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());
        configuration.setClassloader(classloader);
        configuration.setTransactionType(source.getTransactionType());
        configuration.setTransactionHandler(transactionHandler);
        resolveAdministeredObjects(source, configuration);

        WireHolder wireHolder = createWireHolder(wire, source, target, configuration);

        if (source.getMetadata().isResponse()) {
            RequestResponseMessageListener listener = new RequestResponseMessageListener(wireHolder);
            configuration.setMessageListener(listener);
        } else {
            OneWayMessageListener listener = new OneWayMessageListener(wireHolder);
            configuration.setMessageListener(listener);
        }
        try {
            if (jmsHost.isRegistered(configuration.getServiceUri())) {
                // the wire has changed and it is being reprovisioned
                jmsHost.unregisterListener(configuration.getServiceUri());
            }
            jmsHost.registerListener(configuration);
        } catch (JmsHostException e) {
            throw new WiringException(e);
        }
    }

    public void detachFromSource(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        try {
            jmsHost.unregisterListener(target.getUri());
        } catch (JmsHostException e) {
            throw new WiringException(e);
        }
    }

    public void attachObjectFactory(JmsWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition definition)
            throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(JmsWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    private void resolveAdministeredObjects(JmsWireSourceDefinition source, ServiceListenerConfiguration configuration) throws WiringException {
        try {
            JmsBindingMetadata metadata = source.getMetadata();
            Hashtable<String, String> env = metadata.getEnv();
            ConnectionFactoryDefinition requestConnectionFactoryDefinition = metadata.getConnectionFactory();

            checkDefaults(source, requestConnectionFactoryDefinition);

            ConnectionFactory requestConnectionFactory = resolver.resolve(requestConnectionFactoryDefinition, env);
            DestinationDefinition destinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(destinationDefinition, requestConnectionFactory, env);
            configuration.setRequestConnectionFactory(requestConnectionFactory);
            configuration.setRequestDestination(requestDestination);
            if (metadata.isResponse()) {
                ConnectionFactoryDefinition responseConnectionFactoryDefinition = metadata.getResponseConnectionFactory();

                checkDefaults(source, responseConnectionFactoryDefinition);

                ConnectionFactory responseConnectionFactory = resolver.resolve(responseConnectionFactoryDefinition, env);
                DestinationDefinition responseDestinationDestination = metadata.getResponseDestination();
                Destination responseDestination = resolver.resolve(responseDestinationDestination, responseConnectionFactory, env);
                configuration.setResponseConnectionFactory(responseConnectionFactory);
                configuration.setResponseDestination(responseDestination);
            }
        } catch (JmsLookupException e) {
            throw new WiringException(e);
        }
    }

    private WireHolder createWireHolder(Wire wire,
                                        JmsWireSourceDefinition source,
                                        PhysicalWireTargetDefinition target,
                                        ServiceListenerConfiguration configuration) throws WiringException {
        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }

        JmsBindingMetadata metadata = source.getMetadata();
        Map<String, PayloadType> payloadTypes = source.getPayloadTypes();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();
        List<InvocationChainHolder> chainHolders = new ArrayList<InvocationChainHolder>();
        MessageEncoder messageEncoder = null;
        ParameterEncoder parameterEncoder = null;
        String dataBinding = null;
        // FIXME terrible hack to set databinding from operation to wire level
        for (InvocationChain chain : wire.getInvocationChains()) {
            PhysicalOperationDefinition definition = chain.getPhysicalOperation();
            if (definition.getDatabinding() != null) {
                dataBinding = definition.getDatabinding();
            }
            PayloadType payloadType = payloadTypes.get(definition.getName());
            if (payloadType == null) {
                throw new WiringException("Payload type not found for operation: " + definition.getName());
            }
            chainHolders.add(new InvocationChainHolder(chain, payloadType));
        }
        if (dataBinding != null) {
            ParameterEncoderFactory factory = parameterEncoderFactories.get(dataBinding);
            if (factory == null) {
                throw new WiringException("Parameter encoder factory not found for: " + dataBinding);
            }
            try {
                parameterEncoder = factory.getInstance(wire, configuration.getClassloader());
            } catch (EncoderException e) {
                throw new WiringException(e);
            }
            messageEncoder = messageFormatters.get(dataBinding);
            if (messageEncoder == null) {
                throw new WiringException("Message encoder not found for: " + dataBinding);
            }
        }
        return new WireHolder(chainHolders, callbackUri, correlationScheme, configuration.getTransactionType(), messageEncoder, parameterEncoder);
    }

    /**
     * Sets default connection factory values if not specified.
     *
     * @param target                      the source definition
     * @param connectionFactoryDefinition the connection factory definition
     */
    private void checkDefaults(JmsWireSourceDefinition target, ConnectionFactoryDefinition connectionFactoryDefinition) {
        String name = connectionFactoryDefinition.getName();
        if (name == null) {
            if (TransactionType.GLOBAL == target.getTransactionType()) {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_XA_CONNECTION_FACTORY);
            } else {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_CONNECTION_FACTORY);
            }
        }
    }


}
