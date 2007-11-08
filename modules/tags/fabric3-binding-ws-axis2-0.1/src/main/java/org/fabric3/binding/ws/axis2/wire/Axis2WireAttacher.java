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

import org.fabric3.binding.ws.axis2.Axis2ServiceProvisioner;
import org.fabric3.binding.ws.axis2.physical.Axis2WireSourceDefinition;
import org.fabric3.binding.ws.axis2.physical.Axis2WireTargetDefinition;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.WireAttacher;
import org.fabric3.spi.builder.component.WireAttacherRegistry;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2WireAttacher implements WireAttacher<Axis2WireSourceDefinition, Axis2WireTargetDefinition> {
    
    private Axis2ServiceProvisioner serviceProvisioner;
    private WireAttacherRegistry wireAttacherRegistry;
    
    /**
     * @param serviceProvisioner
     * @param wireAttacherRegistry
     */
    public Axis2WireAttacher(@Reference Axis2ServiceProvisioner serviceProvisioner, 
                             @Reference WireAttacherRegistry wireAttacherRegistry) {
        this.serviceProvisioner = serviceProvisioner;
        this.wireAttacherRegistry = wireAttacherRegistry;
    }
    
    /**
     * Registers with the wire attacher registry.
     */
    @Init
    public void start() {
        wireAttacherRegistry.register(Axis2WireSourceDefinition.class, this);
        wireAttacherRegistry.register(Axis2WireTargetDefinition.class, this);
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToSource(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire)
            throws WiringException {
        serviceProvisioner.provision(source, wire);
    }

    /**
     * @see org.fabric3.spi.builder.component.WireAttacher#attachToTarget(org.fabric3.spi.model.physical.PhysicalWireSourceDefinition, 
     *                                                                    org.fabric3.spi.model.physical.PhysicalWireTargetDefinition, 
     *                                                                    org.fabric3.spi.wire.Wire)
     */
    public void attachToTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target, Wire wire)
            throws WiringException {
        
        for (Map.Entry<PhysicalOperationDefinition, InvocationChain> entry : wire.getInvocationChains().entrySet()) {
            Interceptor interceptor = new Axis2TargetInterceptor(target, entry.getKey().getName());
            entry.getValue().addInterceptor(interceptor);
        }
        
    }

}
