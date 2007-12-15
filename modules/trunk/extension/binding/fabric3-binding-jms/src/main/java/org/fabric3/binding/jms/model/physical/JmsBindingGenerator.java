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
import java.util.Set;

import javax.xml.namespace.QName;

import org.fabric3.binding.jms.TransactionType;
import org.fabric3.binding.jms.model.logical.JmsBindingDefinition;
import org.fabric3.extension.generator.BindingGeneratorExtension;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.Constants;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Binding generator that creates the physical source and target definitions for wires. Message 
 * acknowledgement is always expected to be using transactions, either local or global, as expressed by 
 * the intents transactedOneWay, transactedOneWay.local or transactedOneWay.global.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class JmsBindingGenerator extends BindingGeneratorExtension<JmsWireSourceDefinition, JmsWireTargetDefinition, JmsBindingDefinition> {

    // Transacted one way intent
    private static final QName TRANSACTED_ONEWAY = new QName(Constants.SCA_NS, "transactedOneWay");
    private static final QName TRANSACTED_ONEWAY_LOCAL = new QName(Constants.SCA_NS, "transactedOneWay.local");
    private static final QName TRANSACTED_ONEWAY_GLOBAL = new QName(Constants.SCA_NS, "transactedOneWay.global");
    
    /**
     * Classloader generator.
     */
    private ClassLoaderGenerator classLoaderGenerator;

    /**
     * Injects the classloader generator.
     * @param classLoaderGenerator Classloader generator.
     */
    public JmsBindingGenerator(@Reference ClassLoaderGenerator classLoaderGenerator) {
        this.classLoaderGenerator = classLoaderGenerator;
    }
    
    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding,
     *                                                                    java.util.Set,
     *                                                                    org.fabric3.spi.model.type.ServiceDefinition)
     */
    public JmsWireSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Set<Intent> intentsToBeProvided,
                                                      Set<Element> policySetsToBeProvided,
                                                      GeneratorContext context,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        
        TransactionType transactionType = getTransactionType(intentsToBeProvided);
        URI classloader = classLoaderGenerator.generate(logicalBinding, context);
        return new JmsWireSourceDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloader);
        
    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding,
     *                                                                    java.util.Set,
     *                                                                    org.fabric3.spi.model.type.ReferenceDefinition)
     */
    public JmsWireTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      Set<Intent> intentsToBeProvided,
                                                      Set<Element> policySetsToBeProvided,
                                                      GeneratorContext context,
                                                      ReferenceDefinition referenceDefinition) throws GenerationException {
        
        TransactionType transactionType = getTransactionType(intentsToBeProvided);
        URI classloader = classLoaderGenerator.generate(logicalBinding, context);
        return new JmsWireTargetDefinition(logicalBinding.getBinding().getMetadata(), transactionType, classloader);
        
    }

    /**
     * @see org.fabric3.extension.generator.BindingGeneratorExtension#getBindingDefinitionClass()
     */
    @Override
    protected Class<JmsBindingDefinition> getBindingDefinitionClass() {
        return JmsBindingDefinition.class;
    }

    /*
     * Gets the transaction type.
     */
    private TransactionType getTransactionType(Set<Intent> intentsToBeProvided) {
        
        for(Intent intent : intentsToBeProvided) {
            if(TRANSACTED_ONEWAY_GLOBAL.equals(intent.getName())) {
                return TransactionType.GLOBAL;
            } else if(TRANSACTED_ONEWAY_LOCAL.equals(intent.getName())) {
                return TransactionType.LOCAL;
            } else if(TRANSACTED_ONEWAY.equals(intent.getName())) {
                return TransactionType.GLOBAL;
            }
        }

        return null;
        
    }

}
