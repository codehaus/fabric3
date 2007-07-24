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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class representing service contract information
 *
 * @version $Rev$ $Date$
 */
public abstract class ServiceContract<T> extends ModelObject {
    protected boolean conversational;
    protected boolean remotable;
    protected Class<?> interfaceClass;
    protected String interfaceName;
    protected String callbackName;
    protected Class<?> callbackClass;
    protected List<Operation<T>> operations;
    protected List<Operation<T>> callbackOperations;
    protected String dataBinding;
    protected Map<String, Object> metaData;

    protected ServiceContract() {
    }

    protected ServiceContract(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
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
     * Returns the class used to represent the service contract.
     *
     * @return the class used to represent the service contract
     */
    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    /**
     * Sets the class used to represent the service contract.
     *
     * @param interfaceClass the class used to represent the service contract
     */
    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
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
     * Returns the class used to represent the callback service.
     *
     * @return the class used to represent the callback service
     */
    public Class<?> getCallbackClass() {
        return callbackClass;
    }

    /**
     * Sets the class used to represent the callback service.
     *
     * @param callbackClass the class used to represent the callback service
     */
    public void setCallbackClass(Class<?> callbackClass) {
        this.callbackClass = callbackClass;
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

    public String getDataBinding() {
        return dataBinding;
    }

    public void setDataBinding(String dataBinding) {
        this.dataBinding = dataBinding;
    }

    /**
     * Returns a map of metadata key to value mappings for the operation.
     *
     * @return a map of metadata key to value mappings for the operation.
     */
    public Map<String, Object> getMetaData() {
        if (metaData == null) {
            return Collections.emptyMap();
        }
        return metaData;
    }

    /**
     * Adds metadata associated with the operation.
     *
     * @param key the metadata key
     * @param val the metadata value
     */
    public void setMetaData(String key, Object val) {
        if (metaData == null) {
            metaData = new HashMap<String, Object>();
        }
        metaData.put(key, val);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ServiceContract that = (ServiceContract) o;

        if (callbackName != null ? !callbackName.equals(that.callbackName) : that.callbackName != null) {
            return false;
        }
        if (callbackOperations != null ? !callbackOperations.equals(that.callbackOperations)
                : that.callbackOperations != null) {
            return false;
        }
        if (interfaceClass != null ? !interfaceClass.equals(that.interfaceClass) : that.interfaceClass != null) {
            return false;
        }
        //noinspection SimplifiableIfStatement
        if (interfaceName != null ? !interfaceName.equals(that.interfaceName) : that.interfaceName != null) {
            return false;
        }
        return !(operations != null ? !operations.equals(that.operations) : that.operations != null);

    }

    public int hashCode() {
        int result;
        result = interfaceClass != null ? interfaceClass.hashCode() : 0;
        result = 29 * result + (interfaceName != null ? interfaceName.hashCode() : 0);
        result = 29 * result + (callbackName != null ? callbackName.hashCode() : 0);
        result = 29 * result + (operations != null ? operations.hashCode() : 0);
        result = 29 * result + (callbackOperations != null ? callbackOperations.hashCode() : 0);
        return result;
    }

    public String toString() {
        if (interfaceName != null) {
            return new StringBuilder().append("ServiceContract[").append(interfaceName).append("]").toString();
        } else if (interfaceClass != null) {
            return new StringBuilder().append("ServiceContract[").append(interfaceClass.getName()).append("]")
                    .toString();
        } else {
            return super.toString();
        }

    }
}
