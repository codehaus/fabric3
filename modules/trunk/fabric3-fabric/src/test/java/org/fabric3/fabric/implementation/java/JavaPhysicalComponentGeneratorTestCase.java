package org.fabric3.fabric.implementation.java;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.fabric.services.instancefactory.GenerationHelperImpl;
import org.fabric3.pojo.implementation.PojoComponentDefinition;
import org.fabric3.pojo.instancefactory.InjectionSiteMapping;
import org.fabric3.pojo.instancefactory.InstanceFactoryDefinition;
import org.fabric3.pojo.instancefactory.MemberSite;
import org.fabric3.pojo.instancefactory.Signature;
import org.fabric3.pojo.processor.ConstructorDefinition;
import org.fabric3.pojo.processor.JavaMappedProperty;
import org.fabric3.pojo.processor.JavaMappedReference;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.scdl.ComponentDefinition;
import org.fabric3.scdl.CompositeImplementation;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.command.CommandSet;
import org.fabric3.spi.generator.ClassLoaderGenerator;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.model.instance.LogicalComponent;
import org.fabric3.spi.model.instance.ValueSource;
import static org.fabric3.spi.model.instance.ValueSource.ValueSourceType.REFERENCE;
import org.fabric3.spi.model.physical.PhysicalChangeSet;
import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class JavaPhysicalComponentGeneratorTestCase extends TestCase {
    private static final URI COMPONENT_ID = URI.create("component");
    private static final URI RUNTIME_ID = URI.create("runtime1");
    private Method initMethod;
    private Method destroyMethod;

    private JavaComponentGenerator generator;
    private GeneratorContext context;
    private Method setterMethod;

    /**
     * Verifies a physical component definition is properly generated for Java component implementation types
     */
    @SuppressWarnings({"unchecked"})
    public void testGeneration() throws Exception {
        generator.generate(createLogicalComponent(null), context);
        PhysicalChangeSet changeSet = context.getPhysicalChangeSet();
        PhysicalComponentDefinition pDefinition = changeSet.getComponentDefinitions().iterator().next();
        assertTrue(pDefinition instanceof PojoComponentDefinition);
        PojoComponentDefinition pojoDefinition = (PojoComponentDefinition) pDefinition;
        assertEquals(COMPONENT_ID, pojoDefinition.getComponentId());
        InstanceFactoryDefinition provider = pojoDefinition.getInstanceFactoryProviderDefinition();
        assertEquals(Foo.class.getName(), provider.getImplementationClass());

        // verify lifecycle callbacks
        assertEquals(initMethod.getName(), provider.getInitMethod().getName());
        assertEquals(destroyMethod.getName(), provider.getDestroyMethod().getName());

        // verify reference set for the setter
        InjectionSiteMapping mapping = provider.getInjectionSites().get(0);
        MemberSite site = mapping.getSite();
        assertEquals(ElementType.METHOD, site.getElementType());
        assertEquals("setter", site.getName());
        ValueSource source = mapping.getSource();
        assertEquals(REFERENCE, source.getValueType());
        assertEquals("setter", source.getName());

        // verify ctor ref
        ValueSource ctorRefSource = provider.getCdiSources().get(0);
        assertEquals("ctorRef", ctorRefSource.getName());

        // verify ctor prop
        ValueSource ctorPropSource = provider.getCdiSources().get(1);
        assertEquals("ctorProp", ctorPropSource.getName());

/*
        // verify callback
        InjectionSiteMapping callbackMapping = provider.getInjectionSites().get(1);
        MemberSite callbackSite = callbackMapping.getSite();
        assertEquals(ElementType.METHOD, callbackSite.getElementType());
        assertEquals("setCallback", callbackSite.getName());
        ValueSource callbackSource = callbackMapping.getSource();
        assertEquals(SERVICE, callbackSource.getValueType());
        assertEquals("setCallback", callbackSource.getName());
*/

    }


    protected void setUp() throws Exception {
        super.setUp();
        context = new MockContext();
        GeneratorRegistry registry = EasyMock.createNiceMock(GeneratorRegistry.class);
        EasyMock.replay(registry);
        ClassLoaderGenerator classLoaderGenerator = EasyMock.createNiceMock(ClassLoaderGenerator.class);
        EasyMock.replay(classLoaderGenerator);
        generator = new JavaComponentGenerator(registry, classLoaderGenerator, new GenerationHelperImpl());
        initMethod = Foo.class.getMethod("init");
        destroyMethod = Foo.class.getMethod("destroy");
        setterMethod = Foo.class.getMethod("setter", Object.class);

    }

    private LogicalComponent<JavaImplementation> createLogicalComponent(LogicalComponent<CompositeImplementation> parent)
            throws NoSuchMethodException {
        PojoComponentType type = createType();
        JavaImplementation impl = new JavaImplementation();
        impl.setComponentType(type);
        impl.setImplementationClass(Foo.class.getName());
        ComponentDefinition<JavaImplementation> definition =
                new ComponentDefinition<JavaImplementation>(COMPONENT_ID.toString());
        definition.setImplementation(impl);
        definition.setInitLevel(1);
        return new LogicalComponent<JavaImplementation>(COMPONENT_ID,
                                                        RUNTIME_ID,
                                                        definition,
                                                        parent,
                                                        definition.getKey());
    }


    private PojoComponentType createType() throws NoSuchMethodException {
        PojoComponentType type = new PojoComponentType(Foo.class.getName());
        type.setImplementationScope(Scope.COMPOSITE);
        type.setInitMethod(new Signature(initMethod));
        type.setDestroyMethod(new Signature(destroyMethod));

        Constructor<Foo> constructor = Foo.class.getConstructor(Object.class, Object.class);
        ConstructorDefinition<Foo> ctorDef = new ConstructorDefinition<Foo>(constructor);
        ctorDef.getInjectionNames().add("ctorRef");
        ctorDef.getInjectionNames().add("ctorProp");
        type.setConstructorDefinition(ctorDef);

        JavaMappedReference setterReference = new JavaMappedReference("setter", null, setterMethod);
        type.add(setterReference);

        JavaMappedReference ctorReference = new JavaMappedReference("ctorRef", null, null);
        type.add(ctorReference);

        JavaMappedProperty<Object> ctorProperty = new JavaMappedProperty<Object>();
        ctorProperty.setName("ctorProp");
        type.add(ctorProperty);

        return type;
    }

    private static class Foo {

        public Foo(Object ref, Object prop) {
        }

        public void setter(Object ref) {

        }

        public void setCallback(Object callback) {

        }

        public void init() {

        }

        public void destroy() {

        }
    }

    private class MockContext implements GeneratorContext {
        private PhysicalChangeSet changSet = new PhysicalChangeSet();

        public PhysicalChangeSet getPhysicalChangeSet() {
            return changSet;
        }

        public CommandSet getCommandSet() {
            return null;
        }
    }

}
