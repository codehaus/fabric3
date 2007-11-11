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
package org.fabric3.runtime.development.host;

import java.util.Set;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.scdl.definitions.Intent;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalBinding;

/**
 * Implementation of the mock binding generator.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class MockBindingGenerator implements
        BindingGenerator<ClientWireSourceDefinition, MockWireTargetDefinition, MockBindingDefinition> {
    private GeneratorRegistry registry;

    public MockBindingGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(MockBindingDefinition.class, this);
    }

    public ClientWireSourceDefinition generateWireSource(LogicalBinding<MockBindingDefinition> logicalBinding,
                                                         Set<Intent> intentsToBeProvided,
                                                         GeneratorContext context,
                                                         ServiceDefinition serviceDefinition)
            throws GenerationException {
        throw new UnsupportedOperationException();
    }

    public MockWireTargetDefinition generateWireTarget(LogicalBinding<MockBindingDefinition> logicalBinding,
                                                       Set<Intent> intentsToBeProvided,
                                                       GeneratorContext context,
                                                       ReferenceDefinition referenceDefinition)
            throws GenerationException {
        String name = referenceDefinition.getName();
        return new MockWireTargetDefinition(name);
    }
}
