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
    private JmsHost jmsHost;
    private ClassLoaderRegistry classLoaderRegistry;
    private AdministeredObjectResolver resolver;
    private TransactionHandler transactionHandler;
    private Map<String, ParameterEncoderFactory> parameterEncoderFactories = new HashMap<String, ParameterEncoderFactory>();
    private Map<String, MessageEncoder> messageFormatters = new HashMap<String, MessageEncoder>();

    public JmsSourceWireAttacher(@Reference AdministeredObjectResolver resolver,
                                 @Reference TransactionHandler transactionHandler,
                                 @Reference ClassLoaderRegistry classLoaderRegistry,
                                 @Reference JmsHost jmsHost) {
        this.resolver = resolver;
        this.transactionHandler = transactionHandler;
        this.classLoaderRegistry = classLoaderRegistry;
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

        JmsFactory responseJmsFactory = null;
        URI serviceUri = target.getUri();

        ClassLoader cl = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        JmsBindingMetadata metadata = source.getMetadata();
        Hashtable<String, String> env = metadata.getEnv();
        CorrelationScheme correlationScheme = metadata.getCorrelationScheme();
        TransactionType transactionType = source.getTransactionType();

        ConnectionFactoryDefinition connectionFactory = metadata.getConnectionFactory();
        DestinationDefinition destination = metadata.getDestination();
        JmsFactory requestJmsFactory;
        try {
            requestJmsFactory = buildObjectFactory(connectionFactory, destination, env);
            if (metadata.isResponse()) {
                ConnectionFactoryDefinition responseConnectionFactory = metadata.getResponseConnectionFactory();
                DestinationDefinition responseDestination = metadata.getResponseDestination();
                responseJmsFactory = buildObjectFactory(responseConnectionFactory, responseDestination, env);
            }
        } catch (JmsLookupException e) {
            throw new WiringException(e);
        }

        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }

        Map<String, PayloadType> payloadTypes = source.getPayloadTypes();

        WireHolder wireHolder = createWireHolder(wire, payloadTypes, correlationScheme, transactionType, callbackUri, cl);

        ServiceMessageListener messageListener;
        if (metadata.isResponse()) {
            messageListener = new RequestResponseMessageListener(wireHolder);
        } else {
            messageListener = new OneWayMessageListener(wireHolder);
        }
        try {
            if (jmsHost.isRegistered(serviceUri)) {
                // the wire has changed and it is being reprovisioned
                jmsHost.unregisterListener(serviceUri);
            }
            jmsHost.registerResponseListener(requestJmsFactory,
                                             responseJmsFactory,
                                             messageListener,
                                             transactionType,
                                             transactionHandler,
                                             cl,
                                             serviceUri);
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

    private WireHolder createWireHolder(Wire wire,
                                        Map<String, PayloadType> payloadTypes,
                                        CorrelationScheme correlationScheme,
                                        TransactionType transactionType,
                                        String callbackUri,
                                        ClassLoader cl) throws WiringException {
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
                parameterEncoder = factory.getInstance(wire, cl);
            } catch (EncoderException e) {
                throw new WiringException(e);
            }
            messageEncoder = messageFormatters.get(dataBinding);
            if (messageEncoder == null) {
                throw new WiringException("Message encoder not found for: " + dataBinding);
            }
        }
        return new WireHolder(chainHolders, callbackUri, correlationScheme, transactionType, messageEncoder, parameterEncoder);
    }

    private JmsFactory buildObjectFactory(ConnectionFactoryDefinition connectionFactoryDefinition,
                                          DestinationDefinition destinationDefinition,
                                          Hashtable<String, String> env) throws JmsLookupException {
        ConnectionFactory connectionFactory = resolver.resolve(connectionFactoryDefinition, env);
        Destination reqDestination = resolver.resolve(destinationDefinition, connectionFactory, env);
        return new JmsFactory(connectionFactory, reqDestination);
    }

}
