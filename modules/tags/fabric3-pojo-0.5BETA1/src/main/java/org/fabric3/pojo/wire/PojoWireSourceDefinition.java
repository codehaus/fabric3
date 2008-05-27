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
package org.fabric3.pojo.wire;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * @version $Revision$ $Date$
 */
public class PojoWireSourceDefinition extends PhysicalWireSourceDefinition {

    private String interfaceName;
    private URI classLoaderId;

    /**
     * Returns the name of the Java interface for the service contract.
     *
     * @return the name of the Java interface for the service contract
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * Sets the name of the Java interface for the service contract.
     *
     * @param interfaceName the name of the Java interface for the service contract
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * Returns the id of the classloader that should be used to load the interface class.
     *
     * @return the id of the classloader that should be used to load the interface class
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }

    /**
     * Sets the id of the classloader that should be used to load the interface class.
     *
     * @param classLoaderId the id of the classloader that should be used to load the interface class
     */
    public void setClassLoaderId(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

}
