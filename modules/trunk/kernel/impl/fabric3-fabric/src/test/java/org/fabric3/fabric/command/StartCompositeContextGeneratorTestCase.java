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
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.CommandGenerator;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;

/**
 * @version $Rev$ $Date$
 */
public class StartCompositeContextGeneratorTestCase extends TestCase {
    private static final URI PARENT = URI.create("parent");
    private static final URI CHILD = URI.create("parent/child");

    StartCompositeContextGenerator generator;
    LogicalComponent<?> component;

    public void testNestedCompositeGeneration() throws Exception {
        CommandSet commandSet = new CommandSet();
        GeneratorContext context = new DefaultGeneratorContext(null, commandSet);
        generator.generate(component, context);
        List<Command> commands = commandSet.getCommands(CommandSet.Phase.FIRST);
        assertEquals(2, commands.size());
        Command command = commands.get(0);
        assertTrue(command instanceof StartCompositeContextCommand);
        assertEquals("parent/child/", ((StartCompositeContextCommand) command).getGroupId().toString());
        command = commands.get(1);
        assertTrue(command instanceof StartCompositeContextCommand);
        assertEquals("parent/", ((StartCompositeContextCommand) command).getGroupId().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();
        GeneratorRegistry registry = EasyMock.createMock(GeneratorRegistry.class);
        registry.register(EasyMock.isA(CommandGenerator.class));
        EasyMock.replay(registry);
        generator = new StartCompositeContextGenerator(registry);
        ComponentDefinition<CompositeImplementation> parentDefinition = createComposite("parent");
        ComponentDefinition<CompositeImplementation> childDefinition = createComposite("child");

        URI runtimeUri = URI.create("runtime1");
        component = new LogicalComponent<CompositeImplementation>(PARENT, runtimeUri, parentDefinition, null);
        LogicalComponent<?> child =
                new LogicalComponent<CompositeImplementation>(CHILD, runtimeUri, childDefinition, null);
        component.addComponent(child);
    }

    private ComponentDefinition<CompositeImplementation> createComposite(String name) {
        Composite type = new Composite(new QName(name));
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> def = new ComponentDefinition<CompositeImplementation>(name);
        def.setImplementation(impl);


        return def;
    }
}
