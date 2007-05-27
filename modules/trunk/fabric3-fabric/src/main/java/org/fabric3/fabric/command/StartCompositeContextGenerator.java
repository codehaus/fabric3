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
package org.fabric3.fabric.command;

import java.net.URI;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Implementation;

/**
 * Generates a command to start the composite context on a service node
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class StartCompositeContextGenerator implements CommandGenerator {
    private GeneratorRegistry registry;

    public StartCompositeContextGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void generate(LogicalComponent<?> component, GeneratorContext context) throws GenerationException {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        ComponentType<?, ?, ?> type = implementation.getComponentType();
        if (!CompositeComponentType.class.isInstance(type)) {
            return;
        }
        CommandSet commandSet = context.getCommandSet();
        assert commandSet != null;
        // @FIXME a trailing slash is needed since group ids are set on ComponentDefinitions using URI#resolve(",")
        // This should be revisited
        URI groupId = URI.create(component.getUri().toString() + "/");
        commandSet.add(CommandSet.Phase.FIRST, new StartCompositeContextCommand(groupId));
    }
}
