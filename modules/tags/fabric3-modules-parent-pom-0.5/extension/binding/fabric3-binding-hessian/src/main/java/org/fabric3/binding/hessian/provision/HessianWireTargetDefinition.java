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
package org.fabric3.binding.hessian.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

/**
 * Physical wire target definition for Hessian binding.
 * 
 * @version $Revision$ $Date$
 */
public class HessianWireTargetDefinition extends PhysicalWireTargetDefinition {
    private final URI classLoaderId;

    /**
     * Constructor.
     *
     * @param classLoaderId the classloader id to deserialize responses with
     */

    public HessianWireTargetDefinition(URI classLoaderId) {
        this.classLoaderId = classLoaderId;
    }

    /**
     * Returns the classloader id to deserialize responses with.
     *
     * @return the classloader id to deserialize responses with
     */
    public URI getClassLoaderId() {
        return classLoaderId;
    }
}
