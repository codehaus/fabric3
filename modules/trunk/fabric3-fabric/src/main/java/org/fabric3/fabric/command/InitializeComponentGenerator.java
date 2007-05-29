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
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.Implementation;

/**
 * Generates a command to initialize a composite-scoped component on a service node.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class InitializeComponentGenerator implements CommandGenerator {
    private GeneratorRegistry registry;

    public InitializeComponentGenerator(@Reference GeneratorRegistry registry) {
        this.registry = registry;
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public void generate(LogicalComponent<?> component, GeneratorContext context) throws GenerationException {
        if (isComposite(component) || !isEagerInit(component)) {
            // do nothing if a composite or the component is lazy-init
            return;
        }

        CommandSet commandSet = context.getCommandSet();
        assert commandSet != null;
        URI uri = component.getUri();
        // @FIXME a trailing slash is needed since group ids are set on ComponentDefinitions using URI#resolve(",")
        // This should be revisited
        URI groupId = URI.create(component.getParent().getUri().toString() + "/");
        InitializeComponentCommand command = new InitializeComponentCommand(uri, groupId);
        commandSet.add(CommandSet.Phase.LAST, command);
    }

    private boolean isComposite(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        ComponentType<?, ?, ?> type = implementation.getComponentType();
        return CompositeComponentType.class.isInstance(type);
    }

    private boolean isEagerInit(LogicalComponent<?> component) {
        ComponentDefinition<? extends Implementation<?>> definition = component.getDefinition();
        Integer level = definition.getInitLevel();
        if (level != null) {
            return level > 0;
        }
        return definition.getImplementation().getComponentType().getInitLevel() > 0;
    }
}
