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
package org.fabric3.spi.wire;

import java.net.URI;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;

/**
 * The base wire type used to connect references and serviceBindings
 *
 * @version $$Rev$$ $$Date$$
 */
public interface Wire {

    /**
     * Returns the URI of the wire source
     *
     * @return the wire source URI
     */
    URI getSourceUri();

    /**
     * Sets the URI of the wire source
     *
     * @param uri the source uri
     */
    void setSourceUri(URI uri);

    /**
     * Returns the URI of the wire target
     *
     * @return the URI of the wire target
     */
    URI getTargetUri();

    /**
     * Sets the URI of the wire target
     *
     * @param uri the URI of the wire target
     */
    void setTargetUri(URI uri);

    /**
     * Adds the invocation chain associated with the given operation
     *
     * @param operation the service operation
     * @param chain     the invocation chain
     */
    void addInvocationChain(PhysicalOperationDefinition operation, InvocationChain chain);

    Map<PhysicalOperationDefinition, InvocationChain> getInvocationChains();

    Map<PhysicalOperationDefinition, InvocationChain> getCallbackInvocationChains();

    void addCallbackInvocationChain(PhysicalOperationDefinition operation, InvocationChain chain);


}
