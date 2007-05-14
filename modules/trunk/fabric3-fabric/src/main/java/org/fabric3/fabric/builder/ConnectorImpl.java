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
package org.fabric3.fabric.builder;

import java.net.URI;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.wire.InvocationChainImpl;
import org.fabric3.fabric.wire.WireImpl;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The default connector implmentation
 *
 * @version $$Rev$$ $$Date$$
 */
public class ConnectorImpl implements Connector {
    private InterceptorBuilderRegistry interceptorBuilderRegistry;
    private WireAttacherRegistry attacherRegistry;

    @Constructor
    public ConnectorImpl(@Reference InterceptorBuilderRegistry interceptorBuilderRegistry,
                         @Reference WireAttacherRegistry attacherRegistry) {
        this.attacherRegistry = attacherRegistry;
        this.interceptorBuilderRegistry = interceptorBuilderRegistry;
    }

    /**
     * Wires a source and target component based on a wire defintion
     *
     * @param definition the wire definition
     * @throws WiringException
     */
    public void connect(PhysicalWireDefinition definition) throws BuilderException {
        Wire wire = createWire(definition);
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        attacherRegistry.attachToSource(sourceDefinition, targetDefinition, wire);
        attacherRegistry.attachToTarget(sourceDefinition, targetDefinition, wire);
    }

    protected Wire createWire(PhysicalWireDefinition definition) throws BuilderException {
        URI sourceURI = definition.getSourceUri();
        URI targetUri = definition.getTargetUri();
        Wire wire = new WireImpl();
        wire.setSourceUri(sourceURI);
        wire.setTargetUri(targetUri);
        for (PhysicalOperationDefinition operation : definition.getOperations()) {
            InvocationChain chain = new InvocationChainImpl(operation);
            for (PhysicalInterceptorDefinition interceptorDefinition : operation.getInterceptors()) {
                Interceptor interceptor = interceptorBuilderRegistry.build(interceptorDefinition);
                chain.addInterceptor(interceptor);
            }
            wire.addInvocationChain(operation, chain);
        }
        return wire;
    }
}