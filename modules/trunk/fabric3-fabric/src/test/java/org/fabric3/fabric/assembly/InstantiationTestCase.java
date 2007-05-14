package org.fabric3.fabric.assembly;

import java.net.URI;

import junit.framework.TestCase;

import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.model.instance.Referenceable;
import org.fabric3.spi.model.type.ComponentDefinition;
import org.fabric3.spi.model.type.ComponentType;
import org.fabric3.spi.model.type.CompositeComponentType;
import org.fabric3.spi.model.type.CompositeImplementation;
import org.fabric3.spi.model.type.Implementation;
import org.fabric3.spi.model.type.ReferenceDefinition;
import org.fabric3.spi.model.type.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class InstantiationTestCase extends TestCase {
    public static final URI PARENT_URI = URI.create("sca://./domain/parent");
    public static final URI COMPONENT_BASE = URI.create("sca://./domain/parent/component");
    public static final String COMPONENT_URI = PARENT_URI.toString() + "/component";
    public static final String CHILD_URI = COMPONENT_URI + "/child";
    public static final String SERVICE_URI = COMPONENT_URI + "#service";
    public static final String REFERENCE_URI = COMPONENT_URI + "#reference";

    private AbstractAssembly assembly;
    private LogicalComponent<CompositeImplementation> parent;

    public void testInstantiateChildren() throws Exception {
        ComponentDefinition<?> definition = createParentWithChild();
        LogicalComponent<?> logicalComponent = assembly.instantiate(COMPONENT_BASE, parent, definition);
        assertEquals(COMPONENT_URI, logicalComponent.getUri().toString());
        LogicalComponent<?> logicalChild = logicalComponent.getComponent(URI.create(CHILD_URI));
        assertEquals(CHILD_URI, logicalChild.getUri().toString());
    }

    public void testInstantiateServiceReference() throws Exception {
        ComponentDefinition<?> definition = createParentWithServiceAndReference();
        LogicalComponent<?> logicalComponent = assembly.instantiate(COMPONENT_BASE, parent, definition);
        LogicalService logicalService = logicalComponent.getService("service");
        assertEquals(SERVICE_URI, logicalService.getUri().toString());
        LogicalReference logicalReference = logicalComponent.getReference("reference");
        assertEquals(REFERENCE_URI, logicalReference.getUri().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();
        assembly = new MockAssembly();
        parent = new LogicalComponent<CompositeImplementation>(PARENT_URI, null, null);
    }

    private ComponentDefinition<?> createParentWithChild() {
        ComponentType childType = new ComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child =
                new ComponentDefinition<MockImplementation>("child", childImp);

        CompositeComponentType type = new CompositeComponentType();
        type.add(child);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        return new ComponentDefinition<CompositeImplementation>("component", implementation);

    }

    private ComponentDefinition<?> createParentWithServiceAndReference() {
        ServiceDefinition service = new ServiceDefinition();
        service.setUri(URI.create("#service"));
        ReferenceDefinition reference = new ReferenceDefinition();
        reference.setUri(URI.create("#reference"));
        CompositeComponentType type = new CompositeComponentType();
        type.add(service);
        type.add(reference);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        return new ComponentDefinition<CompositeImplementation>("component", implementation);

    }

    private class MockAssembly extends AbstractAssembly {

        public MockAssembly() {
            super(URI.create("sca://./domain"), null, null, null, null);

        }

        protected Referenceable resolveTarget(URI uri, LogicalComponent<CompositeImplementation> component)
                throws ResolutionException {
            return null;
        }

        public void activate(ComponentDefinition<?> definition, boolean include) throws IncludeException {

        }
    }

    private class MockImplementation extends Implementation<ComponentType> {

    }
}
