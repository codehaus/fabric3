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
package org.fabric3.policy;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.ReferenceDefinition;
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.Operation;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.LogicalState;

/**
 * @version $Revision$ $Date$
 */
public class DefaultPolicyResolverTestCase extends TestCase {
    private static final QName POLICY_SET = new QName("urn:test", "testPolicy");
    private LogicalComponent child1;
    private DefaultPolicyResolver resolver;
    private LogicalService child1Service;
    private LogicalReference child1Reference;
    private LogicalBinding child1ReferenceBinding;

    public void testAttachesToComponent() throws Exception {
        resolver.attach(POLICY_SET, child1, true);
        assertEquals(LogicalState.NEW, child1.getState());
        assertTrue(child1.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToComponentIncremental() throws Exception {
        // simulate the policy already being attached and the component deployed
        child1.setState(LogicalState.PROVISIONED);
        child1.addPolicySet(POLICY_SET);
        resolver.attach(POLICY_SET, child1, true);
        assertEquals(LogicalState.PROVISIONED, child1.getState());
    }

    public void testAttachesToComponentNonIncremental() throws Exception {
        child1.setState(LogicalState.PROVISIONED);
        resolver.attach(POLICY_SET, child1, false);
        assertTrue(child1.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToServiceIncremental() throws Exception {
        resolver.attach(POLICY_SET, child1Service, true);
        for (LogicalBinding<?> binding : child1Service.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertTrue(child1Service.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToServiceNonIncremental() throws Exception {
        for (LogicalBinding<?> binding : child1Service.getBindings()) {
            binding.setState(LogicalState.PROVISIONED);
        }
        resolver.attach(POLICY_SET, child1Service, false);
        for (LogicalBinding<?> binding : child1Service.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertTrue(child1Service.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToReferenceIncremental() throws Exception {
        resolver.attach(POLICY_SET, child1Reference, true);
        for (LogicalBinding<?> binding : child1Reference.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertTrue(child1Reference.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToReferenceNonIncremental() throws Exception {
        for (LogicalBinding<?> binding : child1Reference.getBindings()) {
            binding.setState(LogicalState.PROVISIONED);
        }
        resolver.attach(POLICY_SET, child1Reference, false);
        for (LogicalBinding<?> binding : child1Reference.getBindings()) {
            assertEquals(LogicalState.NEW, binding.getState());
        }
        assertTrue(child1Reference.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToBindingIncremental() throws Exception {
        resolver.attach(POLICY_SET, child1ReferenceBinding, true);
        assertEquals(LogicalState.NEW, child1ReferenceBinding.getState());
        assertTrue(child1ReferenceBinding.getPolicySets().contains(POLICY_SET));
    }

    public void testAttachesToBindingNonIncremental() throws Exception {
        child1ReferenceBinding.setState(LogicalState.PROVISIONED);
        resolver.attach(POLICY_SET, child1ReferenceBinding, true);
        assertEquals(LogicalState.NEW, child1ReferenceBinding.getState());
        assertTrue(child1ReferenceBinding.getPolicySets().contains(POLICY_SET));
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createDomain();
        resolver = new DefaultPolicyResolver(null, null, null, null);
    }

    @SuppressWarnings({"unchecked"})
    private void createDomain() {
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        ComponentDefinition definition1 = new ComponentDefinition("child1");
        definition1.setImplementation(new MockImplementation());
        child1 = new LogicalComponent(child1Uri, definition1, domain);
        ServiceContract referenceContract = new MockServiceContract();
        referenceContract.setInterfaceName("ChildService");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("child1Reference", referenceContract);
        child1Reference = new LogicalReference(URI.create("child1#child1Reference"), referenceDefinition, child1);
        BindingDefinition definiton = new MockBindingDefintion();
        child1ReferenceBinding = new LogicalBinding(definiton, child1Reference);
        child1Reference.addBinding(child1ReferenceBinding);
        child1.addReference(child1Reference);
        ServiceContract serviceContract = new MockServiceContract();
        serviceContract.setInterfaceName("ChildService");
        Operation operation = new Operation("operation", null, null, null);
        List<Operation> operations = new ArrayList<Operation>();
        operations.add(operation);
        serviceContract.setOperations(operations);
        ServiceDefinition serviceDefinition = new ServiceDefinition("child1Service", serviceContract);
        child1Service = new LogicalService(URI.create("child1#child1Service"), serviceDefinition, child1);
        child1Service.addPolicySet(POLICY_SET);
        child1Service.addBinding(child1ReferenceBinding);
        child1.addService(child1Service);
        domain.addComponent(child1);
    }

    private class MockBindingDefintion extends BindingDefinition {
        private static final long serialVersionUID = -5325959511447059266L;

        public MockBindingDefintion() {
            super(null, new QName(null, "binding.mock"), null);
        }
    }

    private class MockImplementation extends Implementation {

        private static final long serialVersionUID = 864374876504388888L;

        public QName getType() {
            return new QName(null, "implementation.mock");
        }
    }


    private class MockServiceContract extends ServiceContract {
        private static final long serialVersionUID = -2329187188933582430L;

        public boolean isAssignableFrom(ServiceContract serviceContract) {
            return false;
        }

        public String getQualifiedInterfaceName() {
            return null;
        }
    }

}