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
package org.fabric3.binding.jms.provision;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class JmsWireSourceDefinition extends PhysicalWireSourceDefinition {
    private JmsBindingMetadata metadata;
    private TransactionType transactionType;
    private URI classloaderUri;
    private Set<String> oneWayOperations;
    private Map<String, PayloadType> payloadTypes;

    /**
     * Constructor
     *
     * @param uri              The service URI
     * @param metadata         Metadata to be initialized.
     * @param payloadTypes     The JMS payload types keyed by operation name
     * @param transactionType  Transaction type
     * @param oneWayOperations The set of oneway operation names
     * @param classloaderUri   The classloader URI to deserialize types in
     */
    public JmsWireSourceDefinition(URI uri,
                                   JmsBindingMetadata metadata,
                                   Map<String, PayloadType> payloadTypes,
                                   TransactionType transactionType,
                                   Set<String> oneWayOperations,
                                   URI classloaderUri) {
        this.metadata = metadata;
        this.transactionType = transactionType;
        this.classloaderUri = classloaderUri;
        this.oneWayOperations = oneWayOperations;
        this.payloadTypes = payloadTypes;
        setUri(uri);
    }

    /**
     * Returns the URI of the classloader  to load classes and deserialize parameter types.
     *
     * @return URI of the classloader  to load classes and deserialize parameter types
     */
    public URI getClassloaderUri() {
        return classloaderUri;
    }

    /**
     * @return JMS metadata.
     */
    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    /**
     * Returns the payload type keyed by operation name
     *
     * @return the payload type
     */
    public Map<String, PayloadType> getPayloadTypes() {
        return Collections.unmodifiableMap(payloadTypes);
    }

    /**
     * @return Transaction type.
     */
    public TransactionType getTransactionType() {
        return transactionType;
    }

    /**
     * @param transactionType Transaction type.
     */
    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * Returns the operation names for the wire.
     *
     * @return the operation names for the wire
     */
    public Set<String> getOneWayOperations() {
        return oneWayOperations;
    }

}
