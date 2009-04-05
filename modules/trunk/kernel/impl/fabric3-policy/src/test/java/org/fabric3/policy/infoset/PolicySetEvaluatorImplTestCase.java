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
package org.fabric3.policy.infoset;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.model.type.component.BindingDefinition;
import org.fabric3.model.type.component.ComponentDefinition;
import org.fabric3.model.type.component.CompositeImplementation;
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

/**
 * @version $Revision$ $Date$
 */
public class PolicySetEvaluatorImplTestCase extends TestCase {
    private LogicalComponent child1;
    private LogicalCompositeComponent domain;
    private PolicyEvaluatorImpl evaluator;
    private LogicalComponent child3;

    public void testAttachesToComponent() throws Exception {
        assertTrue(evaluator.doesAttach("component[@name='child1']", child1, domain));
    }

    public void testDoesNotAttachToComponent() throws Exception {
        assertFalse(evaluator.doesAttach("component[@name='child2']", child1, domain));
    }

    public void testAttachesToService() throws Exception {
        assertTrue(evaluator.doesAttach("//component/service[@name='child1Service']", child1, domain));
    }

    public void testAttachesToReference() throws Exception {
        assertTrue(evaluator.doesAttach("//component/reference[@name='child1Reference']", child1, domain));
    }

    public void testAttachesToBindings() throws Exception {
        assertTrue(evaluator.doesAttach("//component/binding.mock", child1, domain));
    }

    public void testAttachesToOperation() throws Exception {
        assertTrue(evaluator.doesAttach("sca:OperationRef('ChildService/operation')", child1, domain));
        assertFalse(evaluator.doesAttach("sca:OperationRef('ChildService/nooperation')", child1, domain));
    }

    public void testAttachesToBindingsForSpecificComponent() throws Exception {
        assertTrue(evaluator.doesAttach("/component[@name='childComposite']//component/binding.mock", child3, domain));
    }

    public void testEvaluateComponentName() throws Exception {
        List<?> result = evaluator.evaluate("component[@name='childComposite']", domain);
        assertEquals(1, result.size());
        assertEquals("childComposite", ((LogicalComponent<?>) result.get(0)).getUri().toString());
    }

    public void testEvaluateBindingWithComponentSelection() throws Exception {
        List<?> results = evaluator.evaluate("//component/binding.mock", domain);
        assertEquals(2, results.size());
        for (Object result : results) {
            assertTrue(result instanceof LogicalBinding);
        }
    }

    public void testEvaluateBinding() throws Exception {
        List<?> results = evaluator.evaluate("//binding.mock", domain);
        assertEquals(2, results.size());
        for (Object result : results) {
            assertTrue(result instanceof LogicalBinding);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        domain = createDomain();
        evaluator = new PolicyEvaluatorImpl();
    }

    @SuppressWarnings({"unchecked"})
    private LogicalCompositeComponent createDomain() {
        LogicalCompositeComponent domain = new LogicalCompositeComponent(URI.create("domain"), null, null);

        URI child1Uri = URI.create("child1");
        ComponentDefinition definition1 = new ComponentDefinition("child1");
        definition1.setImplementation(new MockImplementation());
        child1 = new LogicalComponent(child1Uri, definition1, domain);
        ServiceContract referenceContract = new MockServiceContract();
        referenceContract.setInterfaceName("ChildService");
        ReferenceDefinition referenceDefinition = new ReferenceDefinition("child1Reference", referenceContract);
        LogicalReference reference = new LogicalReference(URI.create("child1#child1Reference"), referenceDefinition, child1);
        BindingDefinition definiton = new MockBindingDefintion();
        LogicalBinding binding = new LogicalBinding(definiton, reference);
        reference.addBinding(binding);
        child1.addReference(reference);
        ServiceContract serviceContract = new MockServiceContract();
        serviceContract.setInterfaceName("ChildService");
        Operation operation = new Operation("operation", null, null, null);
        List<Operation> operations = new ArrayList<Operation>();
        operations.add(operation);
        serviceContract.setOperations(operations);
        ServiceDefinition serviceDefinition = new ServiceDefinition("child1Service", serviceContract);
        LogicalService service = new LogicalService(URI.create("child1#child1Service"), serviceDefinition, child1);
        service.addBinding(binding);
        child1.addService(service);

        URI child2Uri = URI.create("child2");
        ComponentDefinition definition2 = new ComponentDefinition("child2");
        LogicalComponent child2 = new LogicalComponent(child2Uri, definition2, domain);

        URI childCompositeUri = URI.create("childComposite");
        ComponentDefinition<CompositeImplementation> composite = new ComponentDefinition<CompositeImplementation>("childComposite");
        LogicalCompositeComponent childComposite = new LogicalCompositeComponent(childCompositeUri, composite, domain);
        URI child3Uri = URI.create("child3");
        ComponentDefinition definition3 = new ComponentDefinition("child3");
        child3 = new LogicalComponent(child3Uri, definition3, childComposite);
        LogicalReference reference3 = new LogicalReference(URI.create("child3#child1Reference"), referenceDefinition, child3);
        BindingDefinition definiton3 = new MockBindingDefintion();
        LogicalBinding binding3 = new LogicalBinding(definiton3, reference3);
        reference3.addBinding(binding3);
        child3.addReference(reference3);

        childComposite.addComponent(child3);


        domain.addComponent(child1);
        domain.addComponent(child2);
        domain.addComponent(childComposite);
        return domain;
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
