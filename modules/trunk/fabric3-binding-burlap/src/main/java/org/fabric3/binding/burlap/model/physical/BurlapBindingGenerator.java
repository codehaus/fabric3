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
package org.fabric3.binding.burlap.model.physical;

import java.util.Set;

import org.fabric3.binding.burlap.model.logical.BurlapBindingDefinition;
import org.fabric3.extension.generator.BindingGeneratorExtension;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.EagerInit;

/**
 * Implementation of the hessian binding generator.
 * 
 * @version $Revision$ $Date$
 */
@EagerInit
public class BurlapBindingGenerator extends BindingGeneratorExtension<BurlapWireSourceDefinition, BurlapWireTargetDefinition, BurlapBindingDefinition> {

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                    java.util.Set, 
     *                                                                    org.fabric3.scdl.ServiceDefinition)
     */
    public BurlapWireSourceDefinition generateWireSource(LogicalBinding<BurlapBindingDefinition> logicalBinding,
                                                         Set<Intent> intentsToBeProvided,
                                                         GeneratorContext context,
                                                         ServiceDefinition serviceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to physical

        BurlapWireSourceDefinition hwsd = new BurlapWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwsd;

    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                    java.util.Set, 
     *                                                                    org.fabric3.scdl.ReferenceDefinition)
     */
    public BurlapWireTargetDefinition generateWireTarget(LogicalBinding<BurlapBindingDefinition> logicalBinding,
                                                         Set<Intent> intentsToBeProvided,
                                                         GeneratorContext context,
                                                         ReferenceDefinition referenceDefinition)
        throws GenerationException {
        
        // TODO Pass the contract information to the physical

        BurlapWireTargetDefinition hwtd = new BurlapWireTargetDefinition();
        hwtd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwtd;

    }

    /**
     * @see org.fabric3.extension.generator.BindingGeneratorExtension#getBindingDefinitionClass()
     */
    @Override
    protected Class<BurlapBindingDefinition> getBindingDefinitionClass() {
        return BurlapBindingDefinition.class;
    }

}
