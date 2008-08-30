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
package org.fabric3.binding.tcp.control;

import java.net.URI;

import org.fabric3.binding.tcp.provision.TCPWireSourceDefinition;
import org.fabric3.binding.tcp.provision.TCPWireTargetDefinition;
import org.fabric3.binding.tcp.scdl.TCPBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Revision$ $Date$
 */
public class TCPBindingGenerator implements
        BindingGenerator<TCPWireSourceDefinition, TCPWireTargetDefinition, TCPBindingDefinition> {

    /**
     * {@inheritDoc}
     */
    public TCPWireSourceDefinition generateWireSource(LogicalBinding<TCPBindingDefinition> binding, Policy policy,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {

        ServiceContract<?> serviceContract = serviceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI classLoaderId = binding.getParent().getParent().getParent().getUri();
        TCPWireSourceDefinition hwsd = new TCPWireSourceDefinition();
        hwsd.setClassLoaderId(classLoaderId);
        URI targetUri = binding.getBinding().getTargetUri();
        hwsd.setUri(targetUri);

        return hwsd;

    }

    /**
     * {@inheritDoc}
     */
    public TCPWireTargetDefinition generateWireTarget(LogicalBinding<TCPBindingDefinition> binding, Policy policy,
                                                      ReferenceDefinition referenceDefinition)
            throws GenerationException {

        ServiceContract<?> serviceContract = referenceDefinition.getServiceContract();
        if (serviceContract.getOperations().size() != 1) {
            throw new GenerationException("Expects only one operation");
        }

        URI classLoaderId = binding.getParent().getParent().getParent().getUri();

        TCPWireTargetDefinition hwtd = new TCPWireTargetDefinition();
        hwtd.setClassLoaderId(classLoaderId);
        hwtd.setUri(binding.getBinding().getTargetUri());

        return hwtd;

    }

}
