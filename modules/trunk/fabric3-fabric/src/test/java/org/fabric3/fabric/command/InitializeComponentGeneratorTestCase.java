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

import junit.framework.TestCase;

import org.fabric3.fabric.generator.DefaultGeneratorContext;
import org.fabric3.spi.command.Command;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.Property;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class InitializeComponentGeneratorTestCase extends TestCase {
    private static final URI PARENT = URI.create("parent");
    private static final URI COMPONENT = URI.create("parent/component");
    private InitializeComponentGenerator generator;
    private GeneratorContext context;

    public void testGenerationWithSCDLInit() throws Exception {
        LogicalComponent<?> component = createWithSCDLInit();
        generator.generate(component, context);
        Command command = context.getCommandSet().getCommands(CommandSet.Phase.LAST).get(0);
        assertTrue(command instanceof InitializeComponentCommand);
        InitializeComponentCommand initCmd = (InitializeComponentCommand) command;
        assertEquals(COMPONENT, initCmd.getUri());
        assertEquals("parent/", initCmd.getGroupId().toString());
    }

    public void testGenerationWithComponentTypeInit() throws Exception {
        LogicalComponent<?> component = createWithComponentTypeInit();
        generator.generate(component, context);
        Command command = context.getCommandSet().getCommands(CommandSet.Phase.LAST).get(0);
        assertTrue(command instanceof InitializeComponentCommand);
        InitializeComponentCommand initCmd = (InitializeComponentCommand) command;
        assertEquals(COMPONENT, initCmd.getUri());
        assertEquals("parent/", initCmd.getGroupId().toString());
    }

    public void testGenerationNoEagerInit() throws Exception {
        LogicalComponent<?> component = createComponent();
        generator.generate(component, context);
        assertTrue(context.getCommandSet().getCommands(CommandSet.Phase.LAST).isEmpty());
    }

    protected void setUp() throws Exception {
        super.setUp();
        generator = new InitializeComponentGenerator(null);
        CommandSet commandSet = new CommandSet();
        context = new DefaultGeneratorContext(null, commandSet);
    }

    private LogicalComponent createWithSCDLInit() {
        LogicalComponent<?> component = createComponent();
        component.getDefinition().setInitLevel(50);
        return component;
    }

    private LogicalComponent createWithComponentTypeInit() {
        LogicalComponent<?> component = createComponent();
        ComponentDefinition<?> definition = component.getDefinition();
        Implementation<?> implementation = definition.getImplementation();
        implementation.getComponentType().setInitLevel(50);
        return component;
    }

    private LogicalComponent<?> createComponent() {
        ComponentType<?, ?, ?> type = new ComponentType<ServiceDefinition, ReferenceDefinition, Property<?>>();
        MockImplementation impl = new MockImplementation();
        impl.setComponentType(type);
        ComponentDefinition<MockImplementation> def = new ComponentDefinition<MockImplementation>("component", impl);
        LogicalComponent<?> component = new LogicalComponent<MockImplementation>(COMPONENT, COMPONENT, def);
        LogicalComponent<CompositeImplementation> parent =
                new LogicalComponent<CompositeImplementation>(PARENT, PARENT, null);
        component.setParent(parent);
        return component;
    }

    private class MockImplementation extends Implementation<ComponentType<?, ?, ?>> {

    }
}
