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
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Composite;

/**
 * Generates a command to start the composite context on a service node. Child composite contexts will also be started
 * in a depth-first traversal order.
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
        if (!isComposite(component)) {
            return;
        }
        CommandSet commandSet = context.getCommandSet();
        assert commandSet != null;
        addToCommandSet(component, commandSet);
    }

    private void addToCommandSet(LogicalComponent<?> component, CommandSet commandSet) {
        // perform depth-first traversal
        for (LogicalComponent<?> child : component.getComponents()) {
            if (isComposite(child)) {
                addToCommandSet(child, commandSet);
            }
        }
        // @FIXME a trailing slash is needed since group ids are set on ComponentDefinitions using URI#resolve(",")
        // This should be revisited
        URI groupId = URI.create(component.getUri().toString() + "/");
        commandSet.add(CommandSet.Phase.FIRST, new StartCompositeContextCommand(groupId));
    }

    private boolean isComposite(LogicalComponent<?> component) {
        Implementation<?> implementation = component.getDefinition().getImplementation();
        AbstractComponentType<?, ?, ?> type = implementation.getComponentType();
        return Composite.class.isInstance(type);
    }
}
