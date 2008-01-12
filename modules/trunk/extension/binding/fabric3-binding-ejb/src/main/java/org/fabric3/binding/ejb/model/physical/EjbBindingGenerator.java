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
package org.fabric3.binding.ejb.model.physical;

import org.fabric3.binding.ejb.model.logical.EjbBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.PolicyResult;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the EJB binding generator.
 * 
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
@EagerInit
public class EjbBindingGenerator implements
    BindingGenerator<EjbWireSourceDefinition, EjbWireTargetDefinition, EjbBindingDefinition> {

    private final ClassLoaderGenerator classLoaderGenerator;

    /**
     * Injects the generator registry.
     * 
     * @param generatorRegistry Generator registry.
     */
    public EjbBindingGenerator(@Reference GeneratorRegistry generatorRegistry,
                               @Reference ClassLoaderGenerator classLoaderGenerator) {
        generatorRegistry.register(EjbBindingDefinition.class, this);
        this.classLoaderGenerator = classLoaderGenerator;
    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(
     *      org.fabric3.spi.model.instance.LogicalBinding,
     *      java.util.Set,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.scdl.ServiceDefinition)
     */
    public EjbWireSourceDefinition generateWireSource(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      PolicyResult policyResult,
                                                      GeneratorContext generatorContext,
                                                      ServiceDefinition serviceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to physical

        EjbWireSourceDefinition ewsd = new EjbWireSourceDefinition();
        ewsd.setUri(logicalBinding.getBinding().getTargetUri());
        ewsd.setBindingDefinition(logicalBinding.getBinding());
        JavaServiceContract contract = JavaServiceContract.class.cast(serviceDefinition.getServiceContract());
        ewsd.setInterfaceName(contract.getInterfaceClass());
        ewsd.setClassLoaderURI(classLoaderGenerator.generate(logicalBinding, generatorContext));

        return ewsd;
    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(
     *      org.fabric3.spi.model.instance.LogicalBinding,
     *      java.util.Set,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.scdl.ReferenceDefinition)
     */
    public EjbWireTargetDefinition generateWireTarget(LogicalBinding<EjbBindingDefinition> logicalBinding,
                                                      PolicyResult policyResult,
                                                      GeneratorContext generatorContext,
                                                      ReferenceDefinition referenceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to the physical

        EjbWireTargetDefinition ewtd = new EjbWireTargetDefinition();
        ewtd.setUri(logicalBinding.getBinding().getTargetUri());
        ewtd.setBindingDefinition(logicalBinding.getBinding());
        JavaServiceContract contract = JavaServiceContract.class.cast(referenceDefinition.getServiceContract());
        ewtd.setInterfaceName(contract.getInterfaceClass());
        ewtd.setClassLoaderURI(classLoaderGenerator.generate(logicalBinding, generatorContext));

        return ewtd;
    }

}
