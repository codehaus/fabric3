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
package org.fabric3.binding.rmi.model.physical;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.binding.rmi.model.logical.RmiBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

@EagerInit
public class RmiBindingGenerator implements BindingGenerator<RmiWireSourceDefinition, RmiWireTargetDefinition, RmiBindingDefinition> {

    public RmiWireSourceDefinition generateWireSource(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            ServiceDefinition serviceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to physical
        // TODO ignoring intents for now
        RmiWireSourceDefinition ewsd = new RmiWireSourceDefinition();
        ewsd.setUri(logicalBinding.getBinding().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        ewsd.setInterfaceName(contract.getQualifiedInterfaceName());

        URI classloaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        ewsd.setClassLoaderURI(classloaderId);
        return ewsd;

    }

    public RmiWireTargetDefinition generateWireTarget(
            LogicalBinding<RmiBindingDefinition> logicalBinding,
            Policy policy,
            ReferenceDefinition referenceDefinition)
            throws GenerationException {

        // TODO Pass the contract information to the physical

        RmiWireTargetDefinition ewtd = new RmiWireTargetDefinition();
        ewtd.setUri(logicalBinding.getBinding().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        ewtd.setInterfaceName(contract.getQualifiedInterfaceName());

        URI classloaderId = logicalBinding.getParent().getParent().getClassLoaderId();
        ewtd.setClassLoaderURI(classloaderId);
        return ewtd;

    }

}
