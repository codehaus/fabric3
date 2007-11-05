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
package org.fabric3.fabric.wire;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Default implementation of a Wire
 *
 * @version $Rev$ $Date$
 */
public class WireImpl implements Wire {
    private URI sourceUri;
    private URI targetUri;
    private Map<PhysicalOperationDefinition, InvocationChain> chains =
            new HashMap<PhysicalOperationDefinition, InvocationChain>();
    private Map<PhysicalOperationDefinition, InvocationChain> callbackChains =
            new HashMap<PhysicalOperationDefinition, InvocationChain>();

    public WireImpl() {
    }

    public URI getSourceUri() {
        return sourceUri;
    }

    public void setSourceUri(URI sourceUri) {
        this.sourceUri = sourceUri;
    }

    public URI getTargetUri() {
        return targetUri;
    }

    public void setTargetUri(URI targetUri) {
        this.targetUri = targetUri;
    }

    public void addInvocationChain(PhysicalOperationDefinition operation, InvocationChain chain) {
        chains.put(operation, chain);
    }

    public Map<PhysicalOperationDefinition, InvocationChain> getInvocationChains() {
        return Collections.unmodifiableMap(chains);
    }

    public Map<PhysicalOperationDefinition, InvocationChain> getCallbackInvocationChains() {
        return Collections.unmodifiableMap(callbackChains);
    }

    public void addCallbackInvocationChain(PhysicalOperationDefinition operation, InvocationChain chain) {
        callbackChains.put(operation, chain);
    }

}
