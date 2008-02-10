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

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.osoa.sca.annotations.Property;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.scdl.JavaMappedProperty;
import org.fabric3.pojo.scdl.JavaMappedReference;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Multiplicity;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;

/**
 * @version $Rev$ $Date$
 */
public class ConstructorProcessorTestCase extends TestCase {
    private ConstructorProcessor processor =
        new ConstructorProcessor(new ImplementationProcessorServiceImpl(new DefaultContractProcessor()));

    public void testDuplicateConstructor() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        try {
            processor.visitClass(BadFoo.class, type, null);
            fail();
        } catch (DuplicateConstructorException e) {
            // expected
        }
    }

    public void testConstructorAnnotation() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Foo> ctor1 = Foo.class.getConstructor(String.class);
        processor.visitConstructor(ctor1, type, null);
        assertEquals("foo", type.getConstructorDefinition().getInjectionNames().get(0));
    }

    public void testNoAnnotation() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<NoAnnotation> ctor1 = NoAnnotation.class.getConstructor();
        processor.visitConstructor(ctor1, type, null);
        assertNull(type.getConstructorDefinition());
    }

    public void testBadAnnotation() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<BadAnnotation> ctor1 = BadAnnotation.class.getConstructor(String.class, Foo.class);
        try {
            processor.visitConstructor(ctor1, type, null);
            fail();
        } catch (InvalidConstructorException e) {
            // expected
        }
    }

    public void testMixedParameters() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Mixed> ctor1 = Mixed.class.getConstructor(String.class, String.class, String.class);
        processor.visitConstructor(ctor1, type, null);
        assertEquals("_ref0", type.getConstructorDefinition().getInjectionNames().get(0));
        assertEquals("foo", type.getConstructorDefinition().getInjectionNames().get(1));
        assertEquals("bar", type.getConstructorDefinition().getInjectionNames().get(2));
    }

    private static class BadFoo {

        @org.osoa.sca.annotations.Constructor("foo")
        public BadFoo(String foo) {

        }

        @org.osoa.sca.annotations.Constructor({"foo", "bar"})
        public BadFoo(String foo, String bar) {

        }
    }

    private static class Foo {
        @org.osoa.sca.annotations.Constructor("foo")
        public Foo(String foo) {

        }
    }

    private static class NoAnnotation {
        public NoAnnotation() {
        }
    }

    private static class BadAnnotation {
        @org.osoa.sca.annotations.Constructor("foo")
        public BadAnnotation(String foo, Foo ref) {
        }
    }


    public static final class Mixed {
        @org.osoa.sca.annotations.Constructor
        public Mixed(@Reference String param1,
                     @Property(name = "foo")String param2,
                     @Reference(name = "bar")String param3) {
        }
    }

    public static final class Multiple {
        @org.osoa.sca.annotations.Constructor
        public Multiple(@Reference Collection<String> param1,
                        @Property(name = "foo")String[] param2,
                        @Reference(name = "bar", required = false)List<String> param3,
                        @Property(name = "abc")Set<String> param4,
                        @Reference(name = "xyz")String[] param5) {
        }
    }

    public void testMultiplicity() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Multiple> ctor1 =
            Multiple.class.getConstructor(Collection.class, String[].class, List.class, Set.class, String[].class);
        processor.visitConstructor(ctor1, type, null);
        JavaMappedReference ref0 = type.getReferences().get("_ref0");
        assertNotNull(ref0);
        assertEquals(Multiplicity.ONE_N, ref0.getMultiplicity());
        JavaMappedReference ref1 = type.getReferences().get("bar");
        assertNotNull(ref1);
        assertEquals(Multiplicity.ZERO_N, ref1.getMultiplicity());
        JavaMappedReference ref2 = type.getReferences().get("xyz");
        assertNotNull(ref2);
        assertEquals(Multiplicity.ONE_N, ref2.getMultiplicity());
        JavaMappedProperty prop1 = type.getProperties().get("foo");
        assertNotNull(prop1);
        assertTrue(prop1.isMany());
        JavaMappedProperty prop2 = type.getProperties().get("abc");
        assertNotNull(prop2);
        assertTrue(prop2.isMany());
    }

}
