package org.fabric3.fabric.assembly;

import java.net.URI;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.easymock.classextension.EasyMock;
import org.fabric3.fabric.assembly.normalizer.PromotionNormalizer;
import org.fabric3.fabric.model.logical.AtomicComponentInstantiator;
import org.fabric3.fabric.model.logical.CompositeComponentInstantiator;
import org.fabric3.fabric.model.logical.LogicalModelGenerator;
import org.fabric3.fabric.model.logical.LogicalModelGeneratorImpl;
import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.CompositeReference;
import org.fabric3.scdl.CompositeService;
import org.fabric3.scdl.Implementation;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalCompositeComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;
import org.fabric3.spi.wire.WiringService;

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

    private LogicalModelGenerator logicalModelGenerator;
    private LogicalCompositeComponent parent;

    public void testInstantiateChildren() throws Exception {
        ComponentDefinition<?> definition = createParentWithChild();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelGenerator.include(parent, composite);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        assertEquals(COMPONENT_URI, logicalComponent.getUri().toString());
        LogicalComponent<?> logicalChild = logicalComponent.getComponent(URI.create(CHILD_URI));
        assertEquals(CHILD_URI, logicalChild.getUri().toString());
    }

    public void testInstantiateServiceReference() throws Exception {
        ComponentDefinition<?> definition = createParentWithServiceAndReference();
        Composite composite = new Composite(null);
        composite.add(definition);
        logicalModelGenerator.include(parent, composite);
        LogicalCompositeComponent logicalComponent = (LogicalCompositeComponent) parent.getComponents().iterator().next();
        LogicalService logicalService = logicalComponent.getService("service");
        assertEquals(SERVICE_URI, logicalService.getUri().toString());
        LogicalReference logicalReference = logicalComponent.getReference("reference");
        assertEquals(REFERENCE_URI, logicalReference.getUri().toString());
    }


    protected void setUp() throws Exception {
        super.setUp();
        
        AtomicComponentInstantiator atomicComponentInstantiator = new AtomicComponentInstantiator(null);
        CompositeComponentInstantiator compositeComponentInstantiator = new CompositeComponentInstantiator(atomicComponentInstantiator, null);
        WiringService wiringService = EasyMock.createMock(WiringService.class);
        PromotionNormalizer normalizer = EasyMock.createMock(PromotionNormalizer.class);
        
        logicalModelGenerator = new LogicalModelGeneratorImpl(wiringService, normalizer, null, atomicComponentInstantiator, compositeComponentInstantiator);
        parent = new LogicalCompositeComponent(PARENT_URI, null, null, null);
    }

    private ComponentDefinition<?> createParentWithChild() {
        ComponentType childType = new ComponentType();
        MockImplementation childImp = new MockImplementation();
        childImp.setComponentType(childType);
        ComponentDefinition<MockImplementation> child =
                new ComponentDefinition<MockImplementation>("child");
        child.setImplementation(childImp);

        Composite type = new Composite(null);
        type.add(child);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition = 
            new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private ComponentDefinition<?> createParentWithServiceAndReference() {
        CompositeService service = new CompositeService("service", null);
        CompositeReference reference = new CompositeReference("reference", null);
        Composite type = new Composite(null);
        type.add(service);
        type.add(reference);
        CompositeImplementation implementation = new CompositeImplementation();
        implementation.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition = 
            new ComponentDefinition<CompositeImplementation>("component");
        definition.setImplementation(implementation);
        return definition;

    }

    private class MockImplementation extends Implementation<AbstractComponentType<?, ?, ?, ?>> {
        public QName getType() {
            return null;
        }
    }
    
}
