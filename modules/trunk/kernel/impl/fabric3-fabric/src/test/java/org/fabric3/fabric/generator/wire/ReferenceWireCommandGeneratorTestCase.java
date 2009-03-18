/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.fabric.generator.wire;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.command.ReferenceConnectionCommand;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;

/**
 * @version $Revision$ $Date$
 */
public class ReferenceWireCommandGeneratorTestCase extends TestCase {

    private ReferenceWireCommandGenerator generator;
    private PhysicalWireGenerator wireGenerator;

    @SuppressWarnings({"unchecked"})
    public void testIncrementalAttach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);

        wireGenerator.generateBoundReferenceWire(source, reference, binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ReferenceConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testDetach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.MARKED);
        reference.addBinding(binding);

        wireGenerator.generateBoundReferenceWire(source, reference, binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ReferenceConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testReinject() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        reference.addBinding(binding);


        LogicalBinding<?> markedBinding = new LogicalBinding(null, reference, null);
        markedBinding.setState(LogicalState.MARKED);
        reference.addBinding(markedBinding);

        wireGenerator.generateBoundReferenceWire(source, reference, binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));
        wireGenerator.generateBoundReferenceWire(source, reference, markedBinding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(wireGenerator);

        ReferenceConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testNoGeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        source.setState(LogicalState.PROVISIONED);
        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.PROVISIONED);
        reference.addBinding(binding);

        EasyMock.replay(wireGenerator);

        ReferenceConnectionCommand command = generator.generate(source, true);
        assertNull(command);
        EasyMock.verify(wireGenerator);

    }

    @SuppressWarnings({"unchecked"})
    public void testRegeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        source.setState(LogicalState.PROVISIONED);
        LogicalBinding<?> binding = new LogicalBinding(null, reference, null);
        binding.setState(LogicalState.PROVISIONED);
        reference.addBinding(binding);

        wireGenerator.generateBoundReferenceWire(source, reference, binding);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));
        EasyMock.replay(wireGenerator);

        ReferenceConnectionCommand command = generator.generate(source, false);
        assertEquals(1, command.getAttachCommands().size());
        EasyMock.verify(wireGenerator);

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(PhysicalWireGenerator.class);
        generator = new ReferenceWireCommandGenerator(wireGenerator, 0);
    }

    @SuppressWarnings({"SerializableInnerClassWithNonSerializableOuterClass"})
    private class MockContract extends ServiceContract {
        private static final long serialVersionUID = 5001057804874060940L;

        public boolean isAssignableFrom(ServiceContract serviceContract) {
            return false;
        }

        public String getQualifiedInterfaceName() {
            return null;
        }
    }
}