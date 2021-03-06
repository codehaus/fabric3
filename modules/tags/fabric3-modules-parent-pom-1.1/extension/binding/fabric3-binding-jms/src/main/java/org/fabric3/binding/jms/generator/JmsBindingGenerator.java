/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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
package org.fabric3.binding.jms.generator;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.oasisopen.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.binding.jms.model.JmsBindingDefinition;
import org.fabric3.binding.jms.provision.JmsWireSourceDefinition;
import org.fabric3.binding.jms.provision.JmsWireTargetDefinition;
import org.fabric3.binding.jms.provision.PayloadType;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * Binding generator that creates the physical source and target definitions for wires. Message acknowledgement is always expected to be using
 * transactions, either local or global, as expressed by the intents transactedOneWay, transactedOneWay.local or transactedOneWay.global.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator implements BindingGenerator<JmsBindingDefinition> {

    // Transacted one way intent
    private static final QName OASIS_TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName OASIS_TRANSACTED_ONEWAY_LOCAL = new QName(Constants.SCA_NS, "transactedOneWay.local");
    private static final QName OASIS_TRANSACTED_ONEWAY_GLOBAL = new QName(Constants.SCA_NS, "transactedOneWay.global");

    private PayloadTypeIntrospector introspector;

    public JmsBindingGenerator(@Reference PayloadTypeIntrospector introspector) {
        this.introspector = introspector;
    }

    public JmsWireSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        JmsBindingMetadata metadata = logicalBinding.getDefinition().getMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);
        URI uri = logicalBinding.getDefinition().getTargetUri();
        return new JmsWireSourceDefinition(uri, metadata, payloadTypes, transactionType);
    }

    public JmsWireTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        URI uri = logicalBinding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = logicalBinding.getDefinition().getMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);
        return new JmsWireTargetDefinition(uri, metadata, payloadTypes, transactionType);
    }

    /*
     * Gets the transaction type.
     */
    private TransactionType getTransactionType(Policy policy, List<LogicalOperation> operations) {

        // If any operation has the intent, return that
        for (LogicalOperation operation : operations) {
            for (QName intent : policy.getProvidedIntents(operation)) {
                if (OASIS_TRANSACTED_ONEWAY_GLOBAL.equals(intent)) {
                    return TransactionType.GLOBAL;
                } else if (OASIS_TRANSACTED_ONEWAY_LOCAL.equals(intent)) {
                    return TransactionType.LOCAL;
                } else if (OASIS_TRANSACTED_ONEWAY.equals(intent)) {
                    return TransactionType.GLOBAL;
                }
            }
        }
        //no transaction policy specified, use local
        return TransactionType.LOCAL;

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
