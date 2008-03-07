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
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.InjectableAttribute;
import org.fabric3.scdl.ConstructorInjectionSite;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.InjectableAttributeType;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.TypeMapping;

/**
 * @version $Rev$ $Date$
 */
public class ConstructorProcessorTestCase extends TestCase {
    private ConstructorProcessor processor;
    private IntrospectionContext context;
    private PojoComponentType type;

    public void testDuplicateConstructor() throws Exception {
        processor.visitConstructor(BadFoo.class.getConstructor(String.class), type, context);
        try {
            processor.visitConstructor(BadFoo.class.getConstructor(String.class, String.class), type, context);
            fail();
        } catch (DuplicateConstructorException e) {
            // expected
        }
    }

    public void testConstructorAnnotation() throws Exception {
        Constructor<Foo> ctor1 = Foo.class.getConstructor(String.class);
        processor.visitConstructor(ctor1, type, context);
        assertEquals(new Signature(ctor1), type.getConstructor());
    }

    public void testNoAnnotation() throws Exception {
        Constructor<NoAnnotation> ctor1 = NoAnnotation.class.getConstructor();
        processor.visitConstructor(ctor1, type, context);
        assertNull(type.getConstructor());
    }

    public void testBadAnnotation() throws Exception {
        Constructor<BadAnnotation> ctor1 = BadAnnotation.class.getConstructor(String.class, Foo.class);
        try {
            processor.visitConstructor(ctor1, type, context);
            fail();
        } catch (InvalidConstructorException e) {
            // expected
        }
    }

    public void testMixedParameters() throws Exception {
        Constructor<Mixed> ctor1 = Mixed.class.getConstructor(String.class, String.class, String.class);
        processor.visitConstructor(ctor1, type, context);
        assertNotNull(type.getReferences().get("Mixed[0]"));
        assertEquals(new ConstructorInjectionSite(ctor1, 0), type.getInjectionSite(new InjectableAttribute(InjectableAttributeType.REFERENCE, "Mixed[0]")));
        assertNotNull(type.getProperties().get("foo"));
        assertEquals(new ConstructorInjectionSite(ctor1, 1), type.getInjectionSite(new InjectableAttribute(InjectableAttributeType.PROPERTY, "foo")));
        assertNotNull(type.getReferences().get("bar"));
        assertEquals(new ConstructorInjectionSite(ctor1, 2), type.getInjectionSite(new InjectableAttribute(InjectableAttributeType.REFERENCE, "bar")));
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
        Constructor<Multiple> ctor1 = Multiple.class.getConstructor(Collection.class, String[].class, List.class, Set.class, String[].class);
        processor.visitConstructor(ctor1, type, context);

        ReferenceDefinition ref0 = type.getReferences().get("Multiple[0]");
        assertEquals(Multiplicity.ONE_N, ref0.getMultiplicity());
        ReferenceDefinition ref1 = type.getReferences().get("bar");
        assertEquals(Multiplicity.ZERO_N, ref1.getMultiplicity());
        ReferenceDefinition ref2 = type.getReferences().get("xyz");
        assertEquals(Multiplicity.ONE_N, ref2.getMultiplicity());
        org.fabric3.scdl.Property prop1 = type.getProperties().get("foo");
        assertTrue(prop1.isMany());
        org.fabric3.scdl.Property prop2 = type.getProperties().get("abc");
        assertTrue(prop2.isMany());
    }

    protected void setUp() throws Exception {
        super.setUp();
        DefaultIntrospectionHelper helper = new DefaultIntrospectionHelper();
        DefaultContractProcessor contractProcessor = new DefaultContractProcessor(helper);
        processor = new ConstructorProcessor(new ImplementationProcessorServiceImpl(contractProcessor, helper));

        context = EasyMock.createMock(IntrospectionContext.class);
        TypeMapping typeMapping = new TypeMapping();
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);
        type = new PojoComponentType(null);
    }

}
