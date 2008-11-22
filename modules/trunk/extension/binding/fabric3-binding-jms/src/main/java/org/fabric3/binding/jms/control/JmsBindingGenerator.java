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
package org.fabric3.binding.jms.control;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

import static org.osoa.sca.Constants.SCA_NS;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.binding.jms.scdl.JmsBindingDefinition;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

/**
 * Binding generator that creates the physical source and target definitions for wires. Message acknowledgement is always expected to be using
 * transactions, either local or global, as expressed by the intents transactedOneWay, transactedOneWay.local or transactedOneWay.global.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator implements BindingGenerator<JmsWireSourceDefinition, JmsWireTargetDefinition, JmsBindingDefinition> {

    // Transacted one way intent
    private static final QName TRANSACTED_ONEWAY = new QName(SCA_NS, "transactedOneWay");
    private static final QName TRANSACTED_ONEWAY_LOCAL = new QName(SCA_NS, "transactedOneWay.local");
    private static final QName TRANSACTED_ONEWAY_GLOBAL = new QName(SCA_NS, "transactedOneWay.global");
    private static final QName ONEWAY = new QName(SCA_NS, "oneWay");

    private PayloadTypeIntrospector introspector;

    public JmsBindingGenerator(@Reference PayloadTypeIntrospector introspector) {
        this.introspector = introspector;
    }

    public JmsWireSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {

        ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();
        TransactionType transactionType = getTransactionType(policy, serviceContract);
        Set<String> oneWayOperations = getOneWayOperations(policy, serviceContract);

        JmsBindingMetadata metadata = logicalBinding.getDefinition().getMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(serviceContract);
        URI uri = logicalBinding.getDefinition().getTargetUri();
        return new JmsWireSourceDefinition(uri, metadata, payloadTypes, transactionType, oneWayOperations);
    }

    public JmsWireTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition) throws GenerationException {

        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        TransactionType transactionType = getTransactionType(policy, serviceContract);
        Set<String> oneWayOperations = getOneWayOperations(policy, serviceContract);

        URI uri = logicalBinding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = logicalBinding.getDefinition().getMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(serviceContract);
        return new JmsWireTargetDefinition(uri, metadata, payloadTypes, transactionType, oneWayOperations);
    }

    /*
     * Gets the transaction type.
     */
    private TransactionType getTransactionType(Policy policy, ServiceContract<?> serviceContract) {

        // If any operation has the intent, return that
        for (Operation<?> operation : serviceContract.getOperations()) {
            for (QName intent : policy.getProvidedIntents(operation)) {
                if (TRANSACTED_ONEWAY_GLOBAL.equals(intent)) {
                    return TransactionType.GLOBAL;
                } else if (TRANSACTED_ONEWAY_LOCAL.equals(intent)) {
                    return TransactionType.LOCAL;
                } else if (TRANSACTED_ONEWAY.equals(intent)) {
                    return TransactionType.GLOBAL;
                }
            }
        }
        //no transaction policy specified, use local
        return TransactionType.LOCAL;

    }

    /*
     * Gets one way method names.
     */
    private Set<String> getOneWayOperations(Policy policy, ServiceContract<?> serviceContract) {
        Set<String> result = null;
        // If any operation has the intent, return that
        for (Operation<?> operation : serviceContract.getOperations()) {
            for (QName intent : policy.getProvidedIntents(operation)) {
                if (ONEWAY.equals(intent)) {
                    if (result == null) {
                        result = new HashSet<String>();
                    }
                    result.add(operation.getName());
                    break;
                }
            }
        }
        if (result != null) {
            return result;
        } else {
            return Collections.emptySet();
        }
    }

    /**
     * Determines the the payload type to use based on the service contract.
     *
     * @param serviceContract the service contract
     * @return the collection of payload types keyed by operation name
     * @throws JmsGenerationException if an error occurs
     */
    private Map<String, PayloadType> processPayloadTypes(ServiceContract<?> serviceContract) throws JmsGenerationException {
        Map<String, PayloadType> types = new HashMap<String, PayloadType>();
        for (Operation<?> operation : serviceContract.getOperations()) {
            PayloadType payloadType = introspector.introspect(operation);
            types.put(operation.getName(), payloadType);
        }
        return types;
    }
}
