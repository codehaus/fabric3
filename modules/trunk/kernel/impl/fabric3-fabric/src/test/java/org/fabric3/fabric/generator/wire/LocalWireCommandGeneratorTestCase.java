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

import org.fabric3.fabric.command.ConnectionCommand;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
import org.fabric3.model.type.component.Multiplicity;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;
import org.fabric3.spi.model.instance.LogicalWire;
import org.fabric3.spi.model.physical.PhysicalWireDefinition;
import org.fabric3.spi.services.lcm.LogicalComponentManager;

/**
 * @version $Revision$ $Date$
 */
public class LocalWireCommandGeneratorTestCase extends TestCase {

    private LocalWireCommandGenerator generator;
    private PhysicalWireGenerator wireGenerator;
    private LogicalComponentManager lcm;

    @SuppressWarnings({"unchecked"})
    public void testIncrementalAttach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        MockContract contract = new MockContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("source#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        LogicalWire wire = new LogicalWire(composite, reference, URI.create("target#service"));
        composite.addWire(reference, wire);
        composite.addComponent(source);

        lcm.getComponent(targetUri);
        EasyMock.expectLastCall().andReturn(target);

        wireGenerator.generateCollocatedWire(reference, service);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testRegeneration() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        target.setState(LogicalState.PROVISIONED);
        MockContract contract = new MockContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("source#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        source.setState(LogicalState.PROVISIONED);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);
        LogicalWire wire = new LogicalWire(composite, reference, URI.create("target#service"));
        wire.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire);
        composite.addComponent(source);

        lcm.getComponent(targetUri);
        EasyMock.expectLastCall().andReturn(target);

        wireGenerator.generateCollocatedWire(reference, service);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source, false);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(0, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetach() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        MockContract contract = new MockContract();
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("source#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        // mark target to be collected
        target.setState(LogicalState.MARKED);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalWire wire = new LogicalWire(composite, reference, URI.create("target#service"));
        wire.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire);
        composite.addComponent(source);

        lcm.getComponent(targetUri);
        EasyMock.expectLastCall().andReturn(target);

        wireGenerator.generateCollocatedWire(reference, service);
        EasyMock.expectLastCall().andReturn(new PhysicalWireDefinition(null, null, null));

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(lcm, wireGenerator);
        assertEquals(0, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @SuppressWarnings({"unchecked"})
    public void testTargetCollectDetachMultiplicity1ToNReference() throws Exception {
        URI root = URI.create("root");
        ComponentDefinition<CompositeImplementation> definition = new ComponentDefinition<CompositeImplementation>(null);
        LogicalCompositeComponent composite = new LogicalCompositeComponent(root, definition, null);

        MockContract contract = new MockContract();

        URI targetUri2 = URI.create("target2");
        ComponentDefinition<?> targetDefinition2 = new ComponentDefinition(null);
        LogicalComponent<?> target2 = new LogicalComponent(targetUri2, targetDefinition2, composite);
        ServiceDefinition serviceDefinition2 = new ServiceDefinition("service", contract);
        LogicalService service2 = new LogicalService(URI.create("source#service"), serviceDefinition2, target2);
        target2.addService(service2);
        composite.addComponent(target2);

        URI targetUri = URI.create("target");
        ComponentDefinition<?> targetDefinition = new ComponentDefinition(null);
        LogicalComponent<?> target = new LogicalComponent(targetUri, targetDefinition, composite);
        ServiceDefinition serviceDefinition = new ServiceDefinition("service", contract);
        LogicalService service = new LogicalService(URI.create("source#service"), serviceDefinition, target);
        target.addService(service);
        composite.addComponent(target);

        // mark target to be collected
        target.setState(LogicalState.MARKED);

        URI sourceUri = URI.create("source");
        ComponentDefinition<?> sourceDefinition = new ComponentDefinition(null);
        LogicalComponent<?> source = new LogicalComponent(sourceUri, sourceDefinition, composite);
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("reference", contract);
        referenceDefinition.setMultiplicity(Multiplicity.ONE_N);
        LogicalReference reference = new LogicalReference(URI.create("source#reference"), referenceDefinition, source);
        source.addReference(reference);

        LogicalWire wire = new LogicalWire(composite, reference, URI.create("target#service"));
        wire.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire);
        LogicalWire wire2 = new LogicalWire(composite, reference, URI.create("target2#service"));
        wire2.setState(LogicalState.PROVISIONED);
        composite.addWire(reference, wire2);
        composite.addComponent(source);

        lcm.getComponent(targetUri);
        EasyMock.expectLastCall().andReturn(target).times(2);
        lcm.getComponent(targetUri2);
        EasyMock.expectLastCall().andReturn(target2);
        PhysicalWireDefinition wireDefinition = new PhysicalWireDefinition(null, null, null);
        EasyMock.expect(wireGenerator.generateCollocatedWire(reference, service)).andReturn(wireDefinition);
        EasyMock.expect(wireGenerator.generateCollocatedWire(reference, service2)).andReturn(wireDefinition);

        EasyMock.replay(lcm, wireGenerator);

        ConnectionCommand command = generator.generate(source, true);

        EasyMock.verify(lcm, wireGenerator);
        // The generator should create:
        // a. One detach command for target as it was marked
        // b. One attach command for target2. Target2 is still active and the reference needs to be reinjected as it is a multiplicity
        assertEquals(1, command.getAttachCommands().size());
        assertEquals(1, command.getDetachCommands().size());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        wireGenerator = EasyMock.createMock(PhysicalWireGenerator.class);
        lcm = EasyMock.createMock(LogicalComponentManager.class);
        generator = new LocalWireCommandGenerator(wireGenerator, null, lcm, 0);
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
