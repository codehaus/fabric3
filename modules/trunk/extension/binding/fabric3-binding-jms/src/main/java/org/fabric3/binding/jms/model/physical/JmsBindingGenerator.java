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
package org.fabric3.binding.jms.model.physical;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.binding.jms.TransactionType;
import org.fabric3.binding.jms.model.logical.JmsBindingDefinition;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Binding generator that creates the physical source and target definitions for wires. Message acknowledgement is
 * always expected to be using transactions, either local or global, as expressed by the intents transactedOneWay,
 * transactedOneWay.local or transactedOneWay.global.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator implements BindingGenerator<JmsWireSourceDefinition, JmsWireTargetDefinition, JmsBindingDefinition> {

    // Transacted one way intent
    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName TRANSACTED_ONEWAY_LOCAL = new QName(Constants.SCA_NS, "transactedOneWay.local");
    private static final QName TRANSACTED_ONEWAY_GLOBAL = new QName(Constants.SCA_NS, "transactedOneWay.global");


    public JmsWireSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        
        ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();

        TransactionType transactionType = getTransactionType(policy, serviceContract);
        
        URI classloaderId = logicalBinding.getParent().getParent().getParent().getUri();
        return new JmsWireSourceDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloaderId);

    }

    public JmsWireTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition)
            throws GenerationException {
        
        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();

        TransactionType transactionType = getTransactionType(policy, serviceContract);
        
        URI classloaderId = logicalBinding.getParent().getParent().getParent().getUri();
        return new JmsWireTargetDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloaderId);

    }

    /*
     * Gets the transaction type.
     */
    private TransactionType getTransactionType(Policy policy, ServiceContract<?> serviceContract) {
        
        // If any operation has the intent, return that
        for (Operation<?> operation : serviceContract.getOperations()) {
            for (Intent intent : policy.getProvidedIntents(operation)) {
                if (TRANSACTED_ONEWAY_GLOBAL.equals(intent.getName())) {
                    return TransactionType.GLOBAL;
                } else if (TRANSACTED_ONEWAY_LOCAL.equals(intent.getName())) {
                    return TransactionType.LOCAL;
                } else if (TRANSACTED_ONEWAY.equals(intent.getName())) {
                    return TransactionType.GLOBAL;
                }
            }
        }

        return null;

    }

}
