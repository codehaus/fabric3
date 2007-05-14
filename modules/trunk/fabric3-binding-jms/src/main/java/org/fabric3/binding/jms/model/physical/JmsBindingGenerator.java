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
package org.fabric3.binding.jms.model.physical;

import org.fabric3.binding.jms.model.logical.JmsBindingDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Revision$ $Date$
 */
public class JmsBindingGenerator implements
    BindingGenerator<JmsWireSourceDefinition, JmsWireTargetDefinition, JmsBindingDefinition> {

    /**
     * Injects the generator registry.
     * 
     * @param generatorRegistry Generator registry.
     */
    public JmsBindingGenerator(@Reference GeneratorRegistry generatorRegistry) {
        generatorRegistry.register(JmsBindingDefinition.class, this);
    }
    
    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.spi.model.type.ServiceDefinition)
     */
    public JmsWireSourceDefinition generateWireSource(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      GeneratorContext generatorContext,
                                                      ServiceDefinition serviceDefinition) throws GenerationException {
        return new JmsWireSourceDefinition(logicalBinding.getBinding().getMetadata());
    }

    /**
     * @see org.fabric3.spi.generator.BindingGenerator#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding,
     *      org.fabric3.spi.generator.GeneratorContext,
     *      org.fabric3.spi.model.type.ReferenceDefinition)
     */
    public JmsWireTargetDefinition generateWireTarget(LogicalBinding<JmsBindingDefinition> logicalBinding,
                                                      GeneratorContext generatorContext,
                                                      ReferenceDefinition referenceDefinition) throws GenerationException {
        return new JmsWireTargetDefinition(logicalBinding.getBinding().getMetadata());
    }

}
