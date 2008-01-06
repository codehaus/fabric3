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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osoa.sca.annotations.Constructor;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

import org.fabric3.fabric.wire.InvocationChainImpl;
import org.fabric3.fabric.wire.WireImpl;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.ObjectFactory;

/**
 * The default connector implmentation
 *
 * @version $$Rev$$ $$Date$$
 */
@Service(interfaces = {Connector.class, SourceWireAttacherRegistry.class, TargetWireAttacherRegistry.class})
public class ConnectorImpl implements Connector, SourceWireAttacherRegistry, TargetWireAttacherRegistry {
    private InterceptorBuilderRegistry interceptorBuilderRegistry;
    private final Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers;
    private final Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers;

    @Constructor
    public ConnectorImpl(@Reference InterceptorBuilderRegistry interceptorBuilderRegistry) {
        this.interceptorBuilderRegistry = interceptorBuilderRegistry;
        sourceAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>>();
        targetAttachers =
                new ConcurrentHashMap<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>>();
    }

    public <PWSD extends PhysicalWireSourceDefinition> void register(Class<PWSD> type, SourceWireAttacher<PWSD> attacher) {
        sourceAttachers.put(type, attacher);
    }

    public <PWSD extends PhysicalWireSourceDefinition> void unregister(Class<PWSD> type, SourceWireAttacher<PWSD> attacher) {
        sourceAttachers.remove(type);
    }

    public <PWSD extends PhysicalWireTargetDefinition> void register(Class<PWSD> type, TargetWireAttacher<PWSD> attacher) {
        targetAttachers.put(type, attacher);
    }

    public <PWSD extends PhysicalWireTargetDefinition> void unregister(Class<PWSD> type, TargetWireAttacher<PWSD> attacher) {
        targetAttachers.remove(type);
    }

    /**
     * Wires a source and target component based on a wire defintion
     *
     * @param definition the wire definition
     * @throws WiringException
     */
    public void connect(PhysicalWireDefinition definition) throws BuilderException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);

        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = getAttacher(targetDefinition);

        if (definition.isOptimizable()) {
            ObjectFactory<?> objectFactory = targetAttacher.createObjectFactory(targetDefinition);
            sourceAttacher.attachObjectFactory(sourceDefinition, objectFactory);
        } else {
            Wire wire = createWire(definition);
            sourceAttacher.attachToSource(sourceDefinition, targetDefinition, wire);
            targetAttacher.attachToTarget(sourceDefinition, targetDefinition, wire);
        }
    }

    protected Wire createWire(PhysicalWireDefinition definition) throws BuilderException {
        Wire wire = new WireImpl();
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

    @SuppressWarnings("unchecked")
    protected <PWSD extends PhysicalWireSourceDefinition> SourceWireAttacher<PWSD> getAttacher(PWSD source) {
        return (SourceWireAttacher<PWSD>) sourceAttachers.get(source.getClass());
    }

    @SuppressWarnings("unchecked")
    protected <PWSD extends PhysicalWireTargetDefinition> TargetWireAttacher<PWSD> getAttacher(PWSD target) {
        return (TargetWireAttacher<PWSD>) targetAttachers.get(target.getClass());
    }
}