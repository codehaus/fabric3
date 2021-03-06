/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
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
 * ----------------------------------------------------
 *
 * Portions originally based on Apache Tuscany 2007
 * licensed under the Apache 2.0 license.
 *
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
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.JmsTargetDefinition;
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
import org.fabric3.spi.model.physical.PhysicalSourceDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches the reference end of a wire to a JMS queue.
 *
 * @version $Revision$ $Date$
 */
public class JmsTargetWireAttacher implements TargetWireAttacher<JmsTargetDefinition> {
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

    public void attach(PhysicalSourceDefinition source, JmsTargetDefinition target, Wire wire) throws WiringException {

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

    public void detach(PhysicalSourceDefinition source, JmsTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(JmsTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private void resolveAdministeredObjects(JmsTargetDefinition target, WireConfiguration wireConfiguration) throws WiringException {
        JmsBindingMetadata metadata = target.getMetadata();
        Hashtable<String, String> env = metadata.getEnv();

        ConnectionFactoryDefinition connectionFactoryDefinition = metadata.getConnectionFactory();
        checkDefaults(target, connectionFactoryDefinition);

        try {
            ConnectionFactory requestConnectionFactory = resolver.resolve(connectionFactoryDefinition, env);
            DestinationDefinition destinationDefinition = metadata.getDestination();
            Destination requestDestination = resolver.resolve(destinationDefinition, requestConnectionFactory, env);
            wireConfiguration.setRequestConnectionFactory(requestConnectionFactory);
            wireConfiguration.setRequestDestination(requestDestination);
            if (metadata.isResponse()) {
                connectionFactoryDefinition = metadata.getResponseConnectionFactory();
                checkDefaults(target, connectionFactoryDefinition);

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

    /**
     * Sets default connection factory values if not specified.
     *
     * @param target                      the target definition
     * @param connectionFactoryDefinition the connection factory definition
     */
    private void checkDefaults(JmsTargetDefinition target, ConnectionFactoryDefinition connectionFactoryDefinition) {
        String name = connectionFactoryDefinition.getName();
        if (name == null) {
            if (TransactionType.GLOBAL == target.getTransactionType()) {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_XA_CONNECTION_FACTORY);
            } else {
                connectionFactoryDefinition.setName(JmsConstants.DEFAULT_CONNECTION_FACTORY);
            }
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