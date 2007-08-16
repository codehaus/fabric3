/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.fabric.implementation.processor;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Remotable;

import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;
import org.fabric3.pojo.processor.JavaMappedProperty;
import org.fabric3.pojo.processor.JavaMappedReference;
import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class HeuristicConstructorTestCase extends TestCase {

    private HeuristicPojoProcessor processor;

    /**
     * Verifies a single constructor is chosen with a parameter as the type
     */
    public void testSingleConstructorWithParam() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        JavaMappedProperty<String> prop = new JavaMappedProperty<String>();
        prop.setName("foo");
        prop.setJavaType(String.class);
        type.getProperties().put("foo", prop);
        processor.visitEnd(Foo1.class, type, null);
        assertNotNull(type.getConstructorDefinition().getConstructor());
        assertEquals("foo", type.getConstructorDefinition().getInjectionNames().get(0));
    }

    /**
     * Verifies a single constructor is chosen with a reference as the type
     */
    public void testSingleConstructorWithRef() throws Exception {
        PojoComponentType type = new PojoComponentType();
        ServiceContract contract = new JavaServiceContract(String.class);
        JavaMappedReference ref = new JavaMappedReference("foo", contract, null);
        type.getReferences().put("foo", ref);
        processor.visitEnd(Foo1.class, type, null);
        assertNotNull(type.getConstructorDefinition().getConstructor());
        assertEquals("foo", type.getConstructorDefinition().getInjectionNames().get(0));
    }

    /**
     * Verifies a single constructor is chosen with a property and a reference as the type
     */
    public void testSingleConstructorWithPropRef() throws Exception {
        PojoComponentType type = new PojoComponentType();

        JavaMappedProperty<String> prop = new JavaMappedProperty<String>();
        prop.setName("foo");
        prop.setJavaType(String.class);
        type.getProperties().put("foo", prop);

        ServiceContract contract = new JavaServiceContract(Foo1.class);
        JavaMappedReference ref = new JavaMappedReference("ref", contract, null);

        type.getReferences().put("ref", ref);
        processor.visitEnd(Foo2.class, type, null);
        assertNotNull(type.getConstructorDefinition().getConstructor());
        assertEquals(2, type.getConstructorDefinition().getInjectionNames().size());
    }


    public void testSingleConstructorResolvableParam() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo5.class, type, null);
        assertEquals(String.class, type.getProperties().get("string").getJavaType());
    }

    public void testSingleConstructorResolvableRef() throws Exception {
        PojoComponentType type = new PojoComponentType();
        processor.visitEnd(Foo6.class, type, null);
        ServiceContract<?> contract = type.getReferences().get("heuristicconstructortestcase$ref").getServiceContract();
        assertEquals(Ref.class, ((JavaServiceContract) contract).getInterfaceClass());
    }

    public void testSingleConstructorAmbiguousRef() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        ServiceContract contract = new JavaServiceContract(Foo1.class);
        JavaMappedReference ref = new JavaMappedReference("ref", contract, null);
        type.getReferences().put("ref", ref);
        JavaMappedReference ref2 = new JavaMappedReference("ref2", contract, null);
        type.getReferences().put("ref2", ref2);
        try {
            processor.visitEnd(Foo4.class, type, null);
            fail();
        } catch (AmbiguousConstructorException e) {
            // expected
        }
    }

    public void testConstructorPropertyAnnotatedParamsOnly() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo7.class, type, null);
        assertNotNull(type.getProperties().get("myProp"));
    }

    public void testConstructorReferenceAnnotatedParamsOnly() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo8.class, type, null);
        assertNotNull(type.getReferences().get("myRef"));
    }

    @SuppressWarnings("unchecked")
    public void testDefaultConstructor() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo3.class, type, null);
        assertNotNull(type.getConstructorDefinition().getConstructor());
    }

    public void testSameTypesButAnnotated() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo12.class, type, null);
        assertEquals(2, type.getProperties().size());
        assertNotNull(type.getProperties().get("prop1"));
        assertNotNull(type.getProperties().get("prop2"));
    }

    /**
     * Verifies processing executes with additional extension annotations
     */
    public void testRandomAnnotation() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        processor.visitEnd(Foo11.class, type, null);
        assertEquals(1, type.getProperties().size());
        assertNotNull(type.getProperties().get("prop1"));
    }

    public void testPrivateConstructor() throws Exception {
        PojoComponentType type =
                new PojoComponentType();
        try {
            processor.visitEnd(Foo14.class, type, null);
            fail();
        } catch (NoConstructorException e) {
            // expected
        }
    }


    public void testMultipleConstructors() throws Exception {
        //    throw new UnsupportedOperationException("Finish heuristic multiple constructors - Foo10");
    }


    public static class Foo1 {
        public Foo1(String val) {
        }
    }

    public static class Foo2 {
        public Foo2(String val, Foo1 ref) {
        }
    }

    public static class Foo3 {
    }

    public static class Foo4 {
        public Foo4(Foo1 ref) {
        }
    }

    public static class Prop {

    }

    @Remotable
    public static interface Ref {

    }

    public static class Foo5 {
        public Foo5(String val) {
        }
    }

    public static class Foo6 {
        public Foo6(Ref ref) {
        }
    }

    public static class Foo7 {
        public Foo7(@Property(name = "myProp")String prop) {
        }
    }


    public static class Foo8 {
        public Foo8(@Reference(name = "myRef")String ref) {
        }
    }

    public static class Foo9 {
        public Foo9(@Reference(name = "myRef")String ref) {
        }
    }

    public static class Foo10 {

        public Foo10() {
        }

        public Foo10(String prop) {
        }

        public Foo10(@Property(name = "prop1")String prop1, @Property(name = "prop2")String prop2) {

        }
    }

    public static class Foo11 {

        public Foo11(@Property(name = "prop1")String prop, @Baz String baz) {
        }
    }

    public static class Foo12 {

        public Foo12(@Property(name = "prop1")String prop, @Property(name = "prop2")String baz) {
        }
    }

    public @interface Baz {

    }

    public static class Foo13 {
        public Foo13(@Reference String foo) {
        }
    }

    public static final class Foo14 {
        private Foo14() {
        }
    }

    public static final class Foo15 {
        public Foo15(@Reference String param1, @Reference String param2) {
        }
    }

    public static final class Foo16 {
        public Foo16(@Reference String param1,
                     @Property(name = "foo")String param2,
                     @Reference(name = "bar")String param3) {
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        JavaInterfaceProcessorRegistry processorRegistry = new JavaInterfaceProcessorRegistryImpl();
        ImplementationProcessorServiceImpl processorService = new ImplementationProcessorServiceImpl(processorRegistry);
        processor = new HeuristicPojoProcessor(processorService, processorRegistry);
    }
}
