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
package org.fabric3.binding.ejb.control;

import org.fabric3.binding.ejb.provision.EjbWireSourceDefinition;
import org.fabric3.binding.ejb.provision.EjbWireTargetDefinition;
import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.osoa.sca.annotations.EagerInit;

/**
 * Implementation of the EJB binding generator.
 * 
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbBindingGenerator implements BindingGenerator<EjbWireSourceDefinition, EjbWireTargetDefinition, EjbBindingDefinition> {

    public EjbWireSourceDefinition generateWireSource(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ServiceDefinition serviceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to physical

        EjbWireSourceDefinition ewsd = new EjbWireSourceDefinition();
        ewsd.setUri(logicalBinding.getBinding().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = serviceDefinition.getServiceContract();
        ewsd.setInterfaceName(contract.getQualifiedInterfaceName());
        ewsd.setClassLoaderURI(logicalBinding.getParent().getParent().getClassLoaderId());

        return ewsd;
    }

    public EjbWireTargetDefinition generateWireTarget(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      Policy policy,
                                                      ReferenceDefinition referenceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to the physical

        EjbWireTargetDefinition ewtd = new EjbWireTargetDefinition();
        ewtd.setUri(logicalBinding.getBinding().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getBinding());
        ServiceContract<?> contract = referenceDefinition.getServiceContract();
        ewtd.setInterfaceName(contract.getQualifiedInterfaceName());
        ewtd.setClassLoaderURI(logicalBinding.getParent().getParent().getClassLoaderId());

        return ewtd;
    }

}
