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
package org.fabric3.binding.ws.axis2.runtime;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.axis2.runtime.Axis2ServiceProvisioner;
import org.fabric3.binding.ws.axis2.provision.Axis2WireSourceDefinition;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Revision$ $Date$
 *          <p/>
 *          TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2SourceWireAttacher implements SourceWireAttacher<Axis2WireSourceDefinition> {

    private final Axis2ServiceProvisioner serviceProvisioner;

    /**
     * @param serviceProvisioner axis service provisioner
     */
    public Axis2SourceWireAttacher(@Reference Axis2ServiceProvisioner serviceProvisioner) {
        this.serviceProvisioner = serviceProvisioner;
    }

    public void attachToSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        serviceProvisioner.provision(source, wire);
    }

    public void detachFromSource(Axis2WireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        throw new AssertionError();        
    }

    public void attachObjectFactory(Axis2WireSourceDefinition source, ObjectFactory<?> objectFactory) throws WiringException {
        throw new AssertionError();
    }
}
