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
package org.fabric3.scdl;

import java.util.Collections;
import java.util.List;

/**
 * Base class representing service contract information
 *
 * @version $Rev$ $Date$
 */
public abstract class ServiceContract<T> extends ModelObject {
    protected boolean conversational;
    protected boolean remotable;
    protected String interfaceName;
    protected String callbackName;
    protected List<Operation<T>> operations;
    protected List<Operation<T>> callbackOperations;

    protected ServiceContract() {
    }

    /**
     * Returns the interface name for the contract
     *
     * @return the interface name for the contract
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets the interface name for the contract
     *
     * @param interfaceName the interface name
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns true if the service contract is conversational
     *
     * @return true if the service contract is conversational
     */
    public boolean isConversational() {
        return conversational;
    }

    /**
     * Sets if the service contract is conversational
     *
     * @param conversational the conversational attribute
     */
    public void setConversational(boolean conversational) {
        this.conversational = conversational;
    }

    /**
     * Returns true if the contract is remotable.
     *
     * @return the true if the contract is remotable
     */
    public boolean isRemotable() {
        return remotable;
    }

    /**
     * Sets if the contract is remotable
     *
     * @param remotable true if the contract is remotable
     */
    public void setRemotable(boolean remotable) {
        this.remotable = remotable;
    }

    /**
     * Returns the name of the callback or null if the contract is unidirectional.
     *
     * @return the name of the callback or null if the contract is unidirectional
     */
    public String getCallbackName() {
        return callbackName;
    }

    /**
     * Sets the name of the callback service
     *
     * @param callbackName the callback service name
     */
    public void setCallbackName(String callbackName) {
        this.callbackName = callbackName;
    }

    /**
     * Returns the operations for the service contract.
     *
     * @return the operations for the service contract
     */
    public List<Operation<T>> getOperations() {
        if (operations == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(operations);
    }

    /**
     * Sets the operations for the service contract.
     *
     * @param operations the operations for the service contract
     */
    public void setOperations(List<Operation<T>> operations) {
        this.operations = operations;
    }

    /**
     * Returns the callback operations for the service contract.
     *
     * @return the callback operations for the service contract
     */
    public List<Operation<T>> getCallbackOperations() {
        if (callbackOperations == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(callbackOperations);
    }

    /**
     * Sets the callback operations for the service contract.
     *
     * @param callbacksOperations the operations for the service contract
     */
    public void setCallbackOperations(List<Operation<T>> callbacksOperations) {
        this.callbackOperations = callbacksOperations;
    }

    /**
     * Determines if this contract is compatible with the given contract. Compatibility is determined according to the
     * specifics of the IDL's compatibility semantics.
     *
     * @param contract the contract to test compatibility with
     * @return true if the contracts are compatible
     */
    public abstract boolean isAssignableFrom(ServiceContract contract);

    public String toString() {
        if (interfaceName != null) {
            return new StringBuilder().append("ServiceContract[").append(interfaceName).append("]").toString();
        } else {
            return super.toString();
        }

    }
}
