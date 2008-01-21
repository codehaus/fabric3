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
package org.fabric3.binding.burlap.model.physical;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/**
 * Physical wire source definition for Burlap binding.
 *
 * @version $Revision$ $Date$
 */
public class BurlapWireSourceDefinition extends PhysicalWireSourceDefinition {
    private URI classLoaderId;

    /**
     * Constructor.
     *
     * @param classLoaderId the classloader id to deserialize parameters in
     */

    public BurlapWireSourceDefinition(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    /**
     * Returns the classloader id to deserialize parameters in.
     *
     * @return the classloader id to deserialize parameters in
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }
}
