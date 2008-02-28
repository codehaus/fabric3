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
package org.fabric3.binding.aq.model.physical;

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.binding.aq.TransactionType;
import org.fabric3.binding.aq.model.logical.AQBindingDefinition;
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
public class JmsBindingGenerator implements BindingGenerator<JmsWireSourceDefinition, JmsWireTargetDefinition, AQBindingDefinition> {

    // Transacted one way intent
    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName TRANSACTED_ONEWAY_LOCAL = new QName(Constants.SCA_NS, "transactedOneWay.local");
    private static final QName TRANSACTED_ONEWAY_GLOBAL = new QName(Constants.SCA_NS, "transactedOneWay.global");
  
    private GeneratorRegistry generatorRegistry;

    /**
     * Injects the class loader generator.
     *
     * @param classLoaderGenerator Classloader generator.
     * @param generatorRegistry    the generator registry
     */
    public JmsBindingGenerator(@Reference GeneratorRegistry generatorRegistry) {        
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Assign Definition to the registry
     */
    @Init
    public void start() {
        generatorRegistry.register(AQBindingDefinition.class, this);
    }


    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding, org.fabric3.spi.policy.Policy, org.fabric3.scdl.ServiceDefinition)
     */
    public JmsWireSourceDefinition generateWireSource(final LogicalBinding<AQBindingDefinition> logicalBinding,
                                                      final Policy policy,                                                      
                                                      final ServiceDefinition serviceDefinition) throws GenerationException {
        /** Assign service contract and URI Classloader */
        final ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();
        final TransactionType transactionType = getTransactionType(policy, serviceContract);        
        final URI classloader = getClassloaderURI(logicalBinding);
        
        return new JmsWireSourceDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloader);

    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding, org.fabric3.spi.policy.Policy, org.fabric3.scdl.ReferenceDefinition)
     */
    public JmsWireTargetDefinition generateWireTarget(final LogicalBinding<AQBindingDefinition> logicalBinding,
                                                      final Policy policy,                                                      
                                                      final ReferenceDefinition referenceDefinition)throws GenerationException {
        /** Assign service contract and URI Classloader */
        final ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();
        final TransactionType transactionType = getTransactionType(policy, serviceContract);        
        final URI classloader = getClassloaderURI(logicalBinding);
        
        return new JmsWireTargetDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloader);

    }

    /**
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
    
    /**
     * The URI of the Classloader
     * @return URI
     */
    private URI getClassloaderURI(final LogicalBinding<AQBindingDefinition> logicalBinding) {
        return logicalBinding.getParent().getParent().getParent().getUri();
    }
}
