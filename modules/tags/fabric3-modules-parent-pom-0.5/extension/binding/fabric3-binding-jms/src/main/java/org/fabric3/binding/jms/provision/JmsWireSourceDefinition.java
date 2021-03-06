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
import java.util.Set;

import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.TransactionType;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class JmsWireSourceDefinition extends PhysicalWireSourceDefinition {

    /**
     * Metadata.
     */
    private JmsBindingMetadata metadata;

    /**
     * Transaction mode.
     */
    private TransactionType transactionType;

    /**
     * The classloader for the service
     */
    private URI classloaderURI;

    Set<String> oneWayOperations;

    public Set<String> getOneWayOperations() {
        return oneWayOperations;
    }

    public void setOneWayOperations(Set<String> oneWayOperations) {
        this.oneWayOperations = oneWayOperations;
    }
    /**
     * Default constructor.
     */
    public JmsWireSourceDefinition() {
    }

    /**
     * @param metadata Metadata to be initialized.
     * @param transactionType Transaction type.
     */
    public JmsWireSourceDefinition(JmsBindingMetadata metadata, TransactionType transactionType, URI classloaderUri) {
        this.metadata = metadata;
        this.transactionType = transactionType;
        this.classloaderURI = classloaderUri;
    }

    /**
     * @return Classloader UTI.
     */
    public URI getClassloaderURI() {
        return classloaderURI;
    }

    /**
     * @param classloaderURI Classloader URI.
     */
    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }

    /**
     * @return JMS metadata.
     */
    public JmsBindingMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata JMS metadata.
     */
    public void setMetadata(JmsBindingMetadata metadata) {
        this.metadata = metadata;
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



}
