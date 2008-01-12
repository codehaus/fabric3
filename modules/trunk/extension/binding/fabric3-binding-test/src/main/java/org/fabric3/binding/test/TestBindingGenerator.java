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
package org.fabric3.binding.test;

import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.PolicyResult;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * Implementation of the test binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class TestBindingGenerator implements BindingGenerator<TestBindingSourceDefinition, TestBindingTargetDefinition, TestBindingDefinition> {
    private GeneratorRegistry generatorRegistry;

    public TestBindingGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    @Init
    public void start() {
        generatorRegistry.register(TestBindingDefinition.class, this);
    }

    public TestBindingSourceDefinition generateWireSource(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                          PolicyResult policyResult,
                                                          GeneratorContext context,
                                                          ServiceDefinition serviceDefinition)
            throws GenerationException {
        TestBindingSourceDefinition definition = new TestBindingSourceDefinition();
        definition.setUri(logicalBinding.getBinding().getTargetUri());
        return definition;
    }

    public TestBindingTargetDefinition generateWireTarget(LogicalBinding<TestBindingDefinition> logicalBinding,
                                                          PolicyResult policyResult,
                                                          GeneratorContext context,
                                                          ReferenceDefinition referenceDefinition)
            throws GenerationException {

        TestBindingTargetDefinition definition = new TestBindingTargetDefinition();
        definition.setUri(logicalBinding.getBinding().getTargetUri());
        return definition;
    }


}
