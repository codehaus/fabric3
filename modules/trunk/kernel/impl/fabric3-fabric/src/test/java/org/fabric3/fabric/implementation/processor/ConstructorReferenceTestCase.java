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
import java.util.List;

import org.osoa.sca.annotations.Reference;
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.TypeMapping;
import org.fabric3.scdl.ReferenceDefinition;

/**
 * @version $Rev$ $Date$
 */
public class ConstructorReferenceTestCase extends TestCase {
    private ConstructorProcessor processor;
    private IntrospectionContext context;

    public void testReference() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class);
        processor.visitConstructor(ctor, type, context);
        ReferenceDefinition reference = type.getReferences().get("myRef");
        assertTrue(reference.isRequired());
        assertEquals("myRef", reference.getName());
    }

    public void testTwoReferencesSameType() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        processor.visitConstructor(ctor, type, context);
        assertNotNull(type.getReferences().get("myRef1"));
        assertNotNull(type.getReferences().get("myRef2"));
    }

    public void testDuplicateProperty() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<BadFoo> ctor = BadFoo.class.getConstructor(String.class, String.class);
        try {
            processor.visitConstructor(ctor, type, context);
            fail();
        } catch (DuplicateReferenceException e) {
            // expected
        }
    }

    public void testNamesOnConstructor() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(Integer.class);
        processor.visitConstructor(ctor, type, context);
        assertNotNull(type.getReferences().get("myRef"));
    }

    public void testInvalidNumberOfNames() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<BadFoo> ctor = BadFoo.class.getConstructor(Integer.class, Integer.class);
        try {
            processor.visitConstructor(ctor, type, context);
            fail();
        } catch (InvalidConstructorException e) {
            // expected
        }
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
    }

    private static class Foo {

        @org.osoa.sca.annotations.Constructor()
        public Foo(@Reference(name = "myRef", required = true)String prop) {

        }

        @org.osoa.sca.annotations.Constructor()
        public Foo(@Reference(name = "myRef1")String prop1, @Reference(name = "myRef2")String prop2) {

        }

        @org.osoa.sca.annotations.Constructor("myRef")
        public Foo(@Reference Integer prop) {

        }

        @org.osoa.sca.annotations.Constructor()
        public Foo(@Reference List prop) {

        }
    }

    private static class NoNameFoo {

        @org.osoa.sca.annotations.Constructor
        public NoNameFoo(@Reference String prop) {

        }
    }

    private static class BadFoo {

        @org.osoa.sca.annotations.Constructor
        public BadFoo(@Reference(name = "myRef")String prop1, @Reference(name = "myRef")String prop2) {

        }

        @org.osoa.sca.annotations.Constructor
        public BadFoo(@Reference String prop) {

        }

        @org.osoa.sca.annotations.Constructor("myRef")
        public BadFoo(@Reference Integer ref, @Reference Integer ref2) {

        }

        @org.osoa.sca.annotations.Constructor({"myRef", "myRef2"})
        public BadFoo(@Reference List ref, @Reference(name = "myOtherRef")List ref2) {

        }

    }

}
