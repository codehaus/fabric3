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
package org.fabric3.binding.hessian.model.physical;

import org.fabric3.binding.hessian.model.logical.HessianBindingDefinition;
import org.fabric3.extension.generator.BindingGeneratorExtension;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
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
public class HessianBindingGenerator extends BindingGeneratorExtension<HessianWireSourceDefinition, HessianWireTargetDefinition, HessianBindingDefinition> {

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.spi.model.type.ServiceDefinition)
     */
    public HessianWireSourceDefinition generateWireSource(LogicalBinding<HessianBindingDefinition> logicalBinding,
                                                          GeneratorContext generatorContext,
                                                          ServiceDefinition serviceDefinition)
        throws GenerationException {

        HessianWireSourceDefinition hwsd = new HessianWireSourceDefinition();
        hwsd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwsd;

    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.spi.model.type.ReferenceDefinition)
     */
    public HessianWireTargetDefinition generateWireTarget(LogicalBinding<HessianBindingDefinition> logicalBinding,
                                                          GeneratorContext generatorContext,
                                                          ReferenceDefinition referenceDefinition)
        throws GenerationException {

        HessianWireTargetDefinition hwtd = new HessianWireTargetDefinition();
        hwtd.setUri(logicalBinding.getBinding().getTargetUri());

        return hwtd;

    }

    /**
     * @see org.fabric3.extension.generator.BindingGeneratorExtension#getBindingDefinitionClass()
     */
    @Override
    protected Class<HessianBindingDefinition> getBindingDefinitionClass() {
        return HessianBindingDefinition.class;
    }

}
