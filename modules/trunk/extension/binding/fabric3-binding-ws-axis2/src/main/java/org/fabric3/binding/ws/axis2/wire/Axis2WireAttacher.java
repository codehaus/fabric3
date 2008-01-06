/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.binding.ws.axis2.wire;

import java.util.Map;
import java.util.Set;

import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

import org.fabric3.binding.ws.axis2.Axis2ServiceProvisioner;
import org.fabric3.binding.ws.axis2.config.F3Configurator;
import org.fabric3.binding.ws.axis2.physical.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.physical.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.policy.PolicyApplierRegistry;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.builder.component.SourceWireAttacherRegistry;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2WireAttacher implements SourceWireAttacher<Axis2WireSourceDefinition>, TargetWireAttacher<Axis2WireTargetDefinition> {
    private final SourceWireAttacherRegistry sourceWireAttacherRegistry;
    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final Axis2ServiceProvisioner serviceProvisioner;
    private final PolicyApplierRegistry policyApplierRegistry;
    private final F3Configurator f3Configurator;
    
    /**
     * @param sourceWireAttacherRegistry the registry for source wire attachers
     * @param targetWireAttacherRegistry the registry for target wire attachers
     * @param serviceProvisioner
     */
    public Axis2WireAttacher(@Reference Axis2ServiceProvisioner serviceProvisioner, 
                             @Reference SourceWireAttacherRegistry sourceWireAttacherRegistry,
                              @Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                             @Reference PolicyApplierRegistry policyApplierRegistry,
                             @Reference F3Configurator f3Configurator) {
        this.serviceProvisioner = serviceProvisioner;
        this.sourceWireAttacherRegistry = sourceWireAttacherRegistry;
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.policyApplierRegistry = policyApplierRegistry;
        this.f3Configurator = f3Configurator;
    }
    
    /**
     * Registers with the wire attacher registry.
     */
    @Init
    public void start() {
        sourceWireAttacherRegistry.register(Axis2WireSourceDefinition.class, this);
        targetWireAttacherRegistry.register(Axis2WireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        sourceWireAttacherRegistry.unregister(Axis2WireSourceDefinition.class, this);
        targetWireAttacherRegistry.unregister(Axis2WireTargetDefinition.class, this);
    }

    public void attachToSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        serviceProvisioner.provision(source, wire);
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target, Wire wire)
            throws WiringException {
        
        Set<Element> policies = target.getPolicyDefinitions();
        
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            Interceptor interceptor = new Axis2TargetInterceptor(target, 
                                                                 entry.getKey().getName(), 
                                                                 policies, 
                                                                 f3Configurator,
                                                                 policyApplierRegistry);
            entry.getValue().addInterceptor(interceptor);
        }
        
    }

    public void attachObjectFactory(Axis2WireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }

    public ObjectFactory<?> createObjectFactory(Axis2WireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}
