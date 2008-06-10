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
package org.fabric3.binding.aq.provision;

import java.net.URI;

import org.fabric3.binding.aq.common.AQBindingMetadata;
import org.fabric3.binding.aq.common.TransactionType;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class AQWireSourceDefinition extends PhysicalWireSourceDefinition {
    
    /**
     * Metadata.
     */
    private AQBindingMetadata metadata;
    
    /**
     * Transaction mode.
     */
    private TransactionType transactionType;

    /**
     * The classloader for the service
     */
    private URI classloaderURI;
    
    /**
     * Default constructor.
     */
    public AQWireSourceDefinition() {
    }
    
    /**
     * @param metadata Metadata to be initialized.
     * @param transactionType Transaction type.
     */
    public AQWireSourceDefinition(AQBindingMetadata metadata, TransactionType transactionType, URI classloaderUri) {
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
    public AQBindingMetadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata JMS metadata.
     */
    public void setMetadata(AQBindingMetadata metadata) {
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
