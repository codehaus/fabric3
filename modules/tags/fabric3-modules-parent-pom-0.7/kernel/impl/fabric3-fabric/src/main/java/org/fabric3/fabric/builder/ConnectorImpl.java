/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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

import org.osoa.sca.annotations.Reference;

import org.fabric3.fabric.wire.InvocationChainImpl;
import org.fabric3.fabric.wire.WireImpl;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.model.physical.PhysicalInterceptorDefinition;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * The default connector implmentation.
 *
 * @version $$Rev$$ $$Date$$
 */
public class ConnectorImpl implements Connector {
    private Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?, ?>> interceptorBuilders;
    private Map<Class<? extends PhysicalWireSourceDefinition>, SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers;
    private Map<Class<? extends PhysicalWireTargetDefinition>, TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers;

    public ConnectorImpl() {
    }

    @Reference
    public void setInterceptorBuilders(Map<Class<? extends PhysicalInterceptorDefinition>, InterceptorBuilder<?, ?>> interceptorBuilders) {
        this.interceptorBuilders = interceptorBuilders;
    }

    @Reference(required = false)
    public void setSourceAttachers(Map<Class<? extends PhysicalWireSourceDefinition>,
            SourceWireAttacher<? extends PhysicalWireSourceDefinition>> sourceAttachers) {
        this.sourceAttachers = sourceAttachers;
    }

    @Reference
    public void setTargetAttachers(Map<Class<? extends PhysicalWireTargetDefinition>,
            TargetWireAttacher<? extends PhysicalWireTargetDefinition>> targetAttachers) {
        this.targetAttachers = targetAttachers;
    }

    public void connect(PhysicalWireDefinition definition) throws BuilderException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }
        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = getAttacher(targetDefinition);
        if (targetAttacher == null) {
            throw new AttacherNotFoundException("Target attacher not found for type: " + targetDefinition.getClass());
        }

        if (definition.isOptimizable()) {
            ObjectFactory<?> objectFactory = targetAttacher.createObjectFactory(targetDefinition);
            sourceAttacher.attachObjectFactory(sourceDefinition, objectFactory, targetDefinition);
        } else {
            Wire wire = createWire(definition);
            sourceAttacher.attachToSource(sourceDefinition, targetDefinition, wire);
            targetAttacher.attachToTarget(sourceDefinition, targetDefinition, wire);
        }
    }

    public void disconnect(PhysicalWireDefinition definition) throws BuilderException {
        PhysicalWireSourceDefinition sourceDefinition = definition.getSource();
        SourceWireAttacher<PhysicalWireSourceDefinition> sourceAttacher = getAttacher(sourceDefinition);
        if (sourceAttacher == null) {
            throw new AttacherNotFoundException("Source attacher not found for type: " + sourceDefinition.getClass());
        }

        PhysicalWireTargetDefinition targetDefinition = definition.getTarget();
        if (definition.isOptimizable()) {
            sourceAttacher.detachObjectFactory(sourceDefinition, targetDefinition);
        } else {
            TargetWireAttacher<PhysicalWireTargetDefinition> targetAttacher = getAttacher(targetDefinition);
            if (targetAttacher == null) {
                throw new AttacherNotFoundException("Target attacher not found for type: " + targetDefinition.getClass());
            }
            targetAttacher.detachFromTarget(sourceDefinition, targetDefinition);
            sourceAttacher.detachFromSource(sourceDefinition, targetDefinition);
        }
    }

    Wire createWire(PhysicalWireDefinition definition) throws BuilderException {
        Wire wire = new WireImpl();
        for (PhysicalOperationDefinition operation : definition.getOperations()) {
            InvocationChain chain = new InvocationChainImpl(operation);
            for (PhysicalInterceptorDefinition interceptorDefinition : operation.getInterceptors()) {
                InterceptorBuilder<? super PhysicalInterceptorDefinition, ?> builder = getBuilder(interceptorDefinition);
                Interceptor interceptor = builder.build(interceptorDefinition);
                chain.addInterceptor(interceptor);
            }
            wire.addInvocationChain(operation, chain);
        }
        return wire;
    }


    @SuppressWarnings("unchecked")
    protected <PID extends PhysicalInterceptorDefinition> InterceptorBuilder<PID, ?> getBuilder(PID definition) {
        return (InterceptorBuilder<PID, ?>) interceptorBuilders.get(definition.getClass());

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
