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

import org.fabric3.binding.ws.axis2.Axis2ServiceProvisioner;
import org.fabric3.binding.ws.axis2.config.F3Configurator;
import org.fabric3.binding.ws.axis2.physical.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.policy.AxisPolicy;
import org.fabric3.binding.ws.axis2.policy.PolicyApplier;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.builder.component.TargetWireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 *          <p/>
 *          TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2TargetWireAttacher implements TargetWireAttacher<Axis2WireTargetDefinition> {

    private final TargetWireAttacherRegistry targetWireAttacherRegistry;
    private final PolicyApplier policyApplier;
    private final F3Configurator f3Configurator;

    /**
     * @param targetWireAttacherRegistry the registry for target wire attachers
     */
    public Axis2TargetWireAttacher(@Reference TargetWireAttacherRegistry targetWireAttacherRegistry,
                                   @Reference PolicyApplier policyApplier,
                                   @Reference F3Configurator f3Configurator) {
        this.targetWireAttacherRegistry = targetWireAttacherRegistry;
        this.policyApplier = policyApplier;
        this.f3Configurator = f3Configurator;
    }

    /**
     * Registers with the wire attacher registry.
     */
    @Init
    public void start() {
        targetWireAttacherRegistry.register(Axis2WireTargetDefinition.class, this);
    }

    @Destroy
    public void stop() {
        targetWireAttacherRegistry.unregister(Axis2WireTargetDefinition.class, this);
    }

    public void attachToTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target, Wire wire)
            throws WiringException {

        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {

            String operation = entry.getKey().getName();

            Set<AxisPolicy> policies = target.getPolicies(operation);
            Interceptor interceptor = new Axis2TargetInterceptor(target,
                                                                 entry.getKey().getName(),
                                                                 policies,
                                                                 f3Configurator,
                                                                 policyApplier);
            entry.getValue().addInterceptor(interceptor);
        }

    }

    public ObjectFactory<?> createObjectFactory(Axis2WireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }
}