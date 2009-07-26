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
import org.fabric3.binding.jms.provision.JmsSourceDefinition;
import org.fabric3.binding.jms.provision.JmsTargetDefinition;
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

    public JmsSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        JmsBindingMetadata metadata = logicalBinding.getDefinition().getJmsMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);
        URI uri = logicalBinding.getDefinition().getTargetUri();
        return new JmsSourceDefinition(uri, metadata, payloadTypes, transactionType);
    }

    public JmsTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      ServiceContract<?> contract,
                                                      List<LogicalOperation> operations,
                                                      Policy policy) throws GenerationException {

        TransactionType transactionType = getTransactionType(policy, operations);

        URI uri = logicalBinding.getDefinition().getTargetUri();
        JmsBindingMetadata metadata = logicalBinding.getDefinition().getJmsMetadata();
        Map<String, PayloadType> payloadTypes = processPayloadTypes(contract);
        return new JmsTargetDefinition(uri, metadata, payloadTypes, transactionType);
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
