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
import org.fabric3.binding.jms.common.DestinationDefinition;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.runtime.lookup.AdministeredObjectResolver;
import org.fabric3.binding.jms.runtime.lookup.JmsLookupException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.binding.format.MessageEncoder;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the reference end of a wire to a JMS queue.
 *
 * @version $Revision$ $Date$
 */
public class JmsTargetWireAttacher implements TargetWireAttacher<JmsWireTargetDefinition> {
    private AdministeredObjectResolver resolver;
    private ClassLoaderRegistry classLoaderRegistry;
    private Map<String, ParameterEncoderFactory> parameterEncoderFactories = new HashMap<String, ParameterEncoderFactory>();
    private Map<String, MessageEncoder> messageFormatters = new HashMap<String, MessageEncoder>();


    public JmsTargetWireAttacher(@Reference AdministeredObjectResolver resolver, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.resolver = resolver;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference
    public void setParameterEncoderFactories(Map<String, ParameterEncoderFactory> parameterEncoderFactories) {
        this.parameterEncoderFactories = parameterEncoderFactories;
    }

    @Reference
    public void setMessageFormatters(Map<String, MessageEncoder> messageFormatters) {
        this.messageFormatters = messageFormatters;
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, JmsWireTargetDefinition target, Wire wire) throws WiringException {

        WireConfiguration wireConfiguration = new WireConfiguration();
        ClassLoader classloader = classLoaderRegistry.getClassLoader(target.getClassLoaderId());
        wireConfiguration.setClassloader(classloader);
        wireConfiguration.setCorrelationScheme(target.getMetadata().getCorrelationScheme());

        // resolve the connection factories and destinations for the wire
        resolveAdministeredObjects(target, wireConfiguration);

        Map<String, PayloadType> payloadTypes = target.getPayloadTypes();
        for (InvocationChain chain : wire.getInvocationChains()) {
            // setup operation-specific configuration and create an interceptor
            InterceptorConfiguration configuration = new InterceptorConfiguration(wireConfiguration);
            PhysicalOperationDefinition op = chain.getPhysicalOperation();
            String operationName = op.getName();
            configuration.setOperationName(operationName);
            PayloadType payloadType = payloadTypes.get(operationName);
            configuration.setPayloadType(payloadType);
            resolveEncoders(op, wire, classloader, configuration);
            Interceptor interceptor = new JmsInterceptor(configuration);
            chain.addInterceptor(interceptor);
        }

    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, JmsWireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JmsWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void resolveAdministeredObjects(JmsWireTargetDefinition target, WireConfiguration wireConfiguration) throws WiringException {
        JmsBindingMetadata metadata = target.getMetadata();
        Hashtable<String, String> env = metadata.getEnv();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        try {
            ConnectionFactory requestConnectionFactory = resolver.resolve(connectionFactoryDefinition, env);
            DestinationDefinition destinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(destinationDefinition, requestConnectionFactory, env);
            wireConfiguration.setRequestConnectionFactory(requestConnectionFactory);
            wireConfiguration.setRequestDestination(requestDestination);
            if (metadata.isResponse()) {
                connectionFactoryDefinition = metadata.getResponseConnectionFactory();
                ConnectionFactory responseConnectionFactory = resolver.resolve(connectionFactoryDefinition, env);

                destinationDefinition = metadata.getResponseDestination();
                Destination responseDestination = resolver.resolve(destinationDefinition, responseConnectionFactory, env);
                JmsResponseMessageListener receiver = new JmsResponseMessageListener(responseDestination, responseConnectionFactory);
                wireConfiguration.setMessageReceiver(receiver);
            }
        } catch (JmsLookupException e) {
            throw new WiringException(e);
        }

    }

    private void resolveEncoders(PhysicalOperationDefinition op, Wire wire, ClassLoader classloader, InterceptorConfiguration configuration)
            throws WiringException {
        String dataBinding = op.getDatabinding();
        if (dataBinding != null) {
            ParameterEncoderFactory factory = parameterEncoderFactories.get(dataBinding);
            if (factory == null) {
                throw new WiringException("Parameter encoder factory not found for: " + dataBinding);
            }
            try {
                ParameterEncoder parameterEncoder = factory.getInstance(wire, classloader);
                configuration.setParameterEncoder(parameterEncoder);
            } catch (EncoderException e) {
                throw new WiringException(e);
            }
            MessageEncoder messageEncoder = messageFormatters.get(dataBinding);
            if (messageEncoder == null) {
                throw new WiringException("Message encoder not found for: " + dataBinding);
            }
            configuration.setMessageEncoder(messageEncoder);
        }
    }


}