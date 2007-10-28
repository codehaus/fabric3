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
package org.fabric3.binding.ws.axis2.physical;

import java.util.Set;

import org.fabric3.binding.ws.model.logical.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.BindingGeneratorDelegate;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * @version $Revision$ $Date$
 */
public class Axis2BindingGeneratorDelegate implements BindingGeneratorDelegate<WsBindingDefinition> {

    /**
     * @see org.fabric3.spi.generator.BindingGeneratorDelegate#generateWireSource(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                            java.util.Set, 
     *                                                                            org.fabric3.spi.generator.GeneratorContext, 
     *                                                                            org.fabric3.scdl.ServiceDefinition)
     */
    public Axis2WireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                           Set<Intent> intentsToBeProvided, 
                                                           GeneratorContext context, 
                                                           ServiceDefinition serviceDefinition) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see org.fabric3.spi.generator.BindingGeneratorDelegate#generateWireTarget(org.fabric3.spi.model.instance.LogicalBinding, 
     *                                                                            java.util.Set, 
     *                                                                            org.fabric3.spi.generator.GeneratorContext, 
     *                                                                            org.fabric3.scdl.ReferenceDefinition)
     */
    public Axis2WireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Set<Intent> intentsToBeProvided, 
                                                        GeneratorContext context, 
                                                        ReferenceDefinition referenceDefinition) throws GenerationException {
        // TODO Auto-generated method stub
        return null;
    }

}
