package org.fabric3.fabric.assembly.normalizer;

import java.net.URI;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.fabric3.scdl.AbstractComponentType;
import org.fabric3.scdl.BindingDefinition;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.ComponentType;
import org.fabric3.scdl.Composite;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.LogicalReference;
import org.fabric3.spi.model.instance.LogicalService;

/**
 * @version $Rev$ $Date$
 */
public class PromotionNormalizerImplTestCase extends TestCase {
    private PromotionNormalizerImpl normalizer = new PromotionNormalizerImpl();

    public void testServiceNormalize() throws Exception {
        LogicalComponent<?> component = createServiceAssembly();
        normalizer.normalize(component);
        List<LogicalBinding<?>> bindings = component.getServices().iterator().next().getBindings();
        assertEquals(3, bindings.size());
        for (LogicalBinding<?> binding : bindings) {
            BindingDefinition definition = binding.getBinding();
            assertTrue(definition instanceof MockBinding2
                    || definition instanceof MockBinding3
                    || definition instanceof MockBinding4);
        }
    }

    public void testReferenceNormalize() throws Exception {
        LogicalComponent<?> component = createReferenceAssembly();
        normalizer.normalize(component);
        List<LogicalBinding<?>> bindings = component.getReferences().iterator().next().getBindings();
        assertEquals(3, bindings.size());
        for (LogicalBinding<?> binding : bindings) {
            BindingDefinition definition = binding.getBinding();
            assertTrue(definition instanceof MockBinding2
                    || definition instanceof MockBinding3
                    || definition instanceof MockBinding4);
        }
    }

    /**
     * Constucts the following logical assembly:
     * <pre>
     *    Grandparent      Parent        Component
     *    S3-------------->S1----------->S
     *                  /             /
     *    S4-----------/             /
     *                              /
     *                     S2------/
     * <p/>
     * <pre>
     * Service S is promoted a series of times. If each promotion contains a binding configuration, after
     * normalization, the bindings on S should be: S2,S3,S4.
     *
     * @return
     */
    private LogicalComponent<?> createServiceAssembly() {
        //setup grandparent
        LogicalComponent<CompositeImplementation> grandParent = createComposite(URI.create("grandParent"), null);
        // setup parent
        LogicalComponent<CompositeImplementation> parent =
                createComposite(URI.create("grandParent/parent"), grandParent);
        LogicalComponent<?> component = createComponent(URI.create("grandParent/parent/component"), parent);
        parent.addComponent(component);
        grandParent.addComponent(parent);

        ServiceDefinition serviceDefinition1 = new ServiceDefinition();
        LogicalService service1 =
                new LogicalService(URI.create("grandParent/parent#service1"), serviceDefinition1, component);
        LogicalBinding<?> binding1 = new LogicalBinding<BindingDefinition>(new MockBinding(), service1);
        service1.setPromote(URI.create("grandParent/parent/component#service"));
        service1.addBinding(binding1);
        parent.addService(service1);

        ServiceDefinition serviceDefinition2 = new ServiceDefinition();
        LogicalService service2 =
                new LogicalService(URI.create("grandParent/parent#service2"), serviceDefinition2, component);
        LogicalBinding<?> binding2 = new LogicalBinding<BindingDefinition>(new MockBinding2(), service2);
        service2.setPromote(URI.create("grandParent/parent/component#service"));
        service2.addBinding(binding2);
        parent.addService(service2);

        ServiceDefinition serviceDefinition3 = new ServiceDefinition();
        LogicalService service3 = new LogicalService(URI.create("grandParent#service3"), serviceDefinition3, component);
        LogicalBinding<?> binding3 = new LogicalBinding<BindingDefinition>(new MockBinding3(), service3);
        service3.setPromote(URI.create("grandParent/parent#service1"));
        service3.addBinding(binding3);
        grandParent.addService(service3);

        ServiceDefinition serviceDefinition4 = new ServiceDefinition();
        LogicalService service4 = new LogicalService(URI.create("grandParent#service4"), serviceDefinition4, component);
        LogicalBinding<?> binding4 = new LogicalBinding<BindingDefinition>(new MockBinding4(), service4);
        service4.setPromote(URI.create("grandParent/parent#service1"));
        service4.addBinding(binding4);
        grandParent.addService(service4);

        return component;
    }


    /**
     * Constucts the following logical assembly:
     * <pre>
     *    Component       Parent         Grandparent
     *    R-------------->R1------------->R3
     *      \               \------------>R4
     *       \
     *        \---------->R2
     * <p/>
     * <pre>
     * Reference R is promoted a series of times. If each promotion contains a binding configuration, after
     * normalization, the bindings on R should be: R2,R3,R4.
     *
     * @return
     */
    private LogicalComponent<?> createReferenceAssembly() {
        //setup grandparent
        LogicalComponent<CompositeImplementation> grandParent = createComposite(URI.create("grandParent"), null);
        // setup parent
        LogicalComponent<CompositeImplementation> parent =
                createComposite(URI.create("grandParent/parent"), grandParent);
        LogicalComponent<?> component = createComponent(URI.create("grandParent/parent/component"), parent);
        parent.addComponent(component);
        grandParent.addComponent(parent);

        ReferenceDefinition refDefinition1 = new ReferenceDefinition(null, null);
        LogicalReference reference1 =
                new LogicalReference(URI.create("grandParent/parent#reference1"), refDefinition1, component);
        LogicalBinding<?> binding1 = new LogicalBinding<BindingDefinition>(new MockBinding(), reference1);
        reference1.addPromotedUri(URI.create("grandParent/parent/component#reference"));
        reference1.addBinding(binding1);
        parent.addReference(reference1);

        ReferenceDefinition refDeinition2 = new ReferenceDefinition(null, null);
        LogicalReference reference2 =
                new LogicalReference(URI.create("grandParent/parent#reference2"), refDeinition2, component);
        LogicalBinding<?> binding2 = new LogicalBinding<BindingDefinition>(new MockBinding2(), reference2);
        reference2.addPromotedUri(URI.create("grandParent/parent/component#reference"));
        reference2.addBinding(binding2);
        parent.addReference(reference2);

        ReferenceDefinition refDefinition3 = new ReferenceDefinition(null, null);
        LogicalReference reference3 =
                new LogicalReference(URI.create("grandParent#reference3"), refDefinition3, component);
        LogicalBinding<?> binding3 = new LogicalBinding<BindingDefinition>(new MockBinding3(), reference3);
        reference3.addPromotedUri(URI.create("grandParent/parent#reference1"));
        reference3.addBinding(binding3);
        grandParent.addReference(reference3);

        ReferenceDefinition refDefinition4 = new ReferenceDefinition(null, null);
        LogicalReference reference4 =
                new LogicalReference(URI.create("grandParent#reference4"), refDefinition4, component);
        LogicalBinding<?> binding4 = new LogicalBinding<BindingDefinition>(new MockBinding4(), reference4);
        reference4.addPromotedUri(URI.create("grandParent/parent#reference1"));
        reference4.addBinding(binding4);
        grandParent.addReference(reference4);

        return component;
    }

    private LogicalComponent<CompositeImplementation> createComposite(URI uri, LogicalComponent<CompositeImplementation> parent) {
        URI runtimeID = URI.create("id");
        Composite type = new Composite(null);
        //parentType.add();
        CompositeImplementation impl = new CompositeImplementation();
        impl.setComponentType(type);
        ComponentDefinition<CompositeImplementation> definition =
                new ComponentDefinition<CompositeImplementation>(uri.toString());
        definition.setImplementation(impl);
        return new LogicalComponent<CompositeImplementation>(uri, runtimeID, definition, parent, definition.getKey());

    }

    private LogicalComponent<?> createComponent(URI uri, LogicalComponent<CompositeImplementation> parent) {
        URI runtimeID = URI.create("id");
        ComponentType type = new ComponentType();
        MockImplementation impl = new MockImplementation();
        impl.setComponentType(type);
        ComponentDefinition<MockImplementation> definition =
                new ComponentDefinition<MockImplementation>(uri.toString());
        definition.setImplementation(impl);
        LogicalComponent<MockImplementation> component =
                new LogicalComponent<MockImplementation>(uri, runtimeID, definition, parent, definition.getKey());
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        LogicalService service =
                new LogicalService(URI.create("grandParent/parent/component#service"), serviceDefinition, component);
        component.addService(service);

        ReferenceDefinition referenceDefinition = new ReferenceDefinition(null, null);
        LogicalReference reference =
                new LogicalReference(URI.create("grandParent/parent/component#reference"),
                                     referenceDefinition,
                                     component);
        component.addReference(reference);
        return component;
    }

    private class MockImplementation extends Implementation<AbstractComponentType<?, ?, ?>> {
        public QName getType() {
            throw new UnsupportedOperationException();
        }

    }

    private class MockBinding extends BindingDefinition {
        private MockBinding() {
            super(null);
        }
    }

    private class MockBinding2 extends BindingDefinition {
        private MockBinding2() {
            super(null);
        }
    }

    private class MockBinding3 extends BindingDefinition {
        private MockBinding3() {
            super(null);
        }
    }

    private class MockBinding4 extends BindingDefinition {
        private MockBinding4() {
            super(null);
        }
    }
}
