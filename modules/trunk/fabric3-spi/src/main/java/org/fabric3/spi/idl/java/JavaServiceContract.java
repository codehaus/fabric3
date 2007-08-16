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
package org.fabric3.spi.idl.java;

import java.lang.reflect.Type;

import org.fabric3.scdl.ServiceContract;

/**
 * Represents a service contract specified using a Java interface
 *
 * @version $Rev$ $Date$
 */
public class JavaServiceContract extends ServiceContract<Type> {
    private String interfaceClass;
    private String callbackClass;

    public JavaServiceContract(String interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    /**
     * Returns the fully qualified class name used to represent the service contract.
     *
     * @return the class name used to represent the service contract
     */
    public String getInterfaceClass() {
        return interfaceClass;
    }

    /**
     * Returns the fully qualified class name used to represent the callback service.
     *
     * @return the class name used to represent the callback service
     */
    public String getCallbackClass() {
        return callbackClass;
    }

    /**
     * Sets the class used to represent the callback service.
     *
     * @param callbackClass the class used to represent the callback service
     */
    public void setCallbackClass(String callbackClass) {
        this.callbackClass = callbackClass;
    }

    public boolean isAssignableFrom(ServiceContract<?> contract) {
        if (JavaServiceContract.class.isInstance(contract)) {
            // short-circuit test if both are JavaClasses
            JavaServiceContract jContract = JavaServiceContract.class.cast(contract);
            Class<?> thisClass;
            Class<?> otherClass;
            try {
                // load classes dynamically since the model cannot reference them directly as they may not be on the
                // classpath at al times, e.g. if the model is being deserialized.
                thisClass = Class.forName(interfaceClass);
                otherClass = Class.forName(jContract.interfaceClass);
            } catch (ClassNotFoundException e) {
                throw new AssertionError(e);
            }
            if (thisClass.isAssignableFrom(otherClass)) {
                return true;
            }
        }
        // TODO handle the case where the contract is defined using a different IDL
        return false;
    }

}
