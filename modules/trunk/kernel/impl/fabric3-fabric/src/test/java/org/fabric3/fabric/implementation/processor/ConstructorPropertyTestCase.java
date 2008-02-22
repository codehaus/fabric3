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

import org.osoa.sca.annotations.Property;
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.TypeMapping;

/**
 * @version $Rev$ $Date$
 */
public class ConstructorPropertyTestCase extends TestCase {

    private ConstructorProcessor processor;
    private IntrospectionContext context;

    public void testProperty() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class);
        processor.visitConstructor(ctor, type, context);
        org.fabric3.scdl.Property property = type.getProperties().get("myProp");
        assertTrue(property.isRequired());
        assertEquals("myProp", property.getName());
    }

    public void testTwoPropertiesSameType() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        processor.visitConstructor(ctor, type, context);
        assertNotNull(type.getProperties().get("myProp1"));
        assertNotNull(type.getProperties().get("myProp2"));
    }

    public void testDuplicateProperty() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<BadFoo> ctor = BadFoo.class.getConstructor(String.class, String.class);
        try {
            processor.visitConstructor(ctor, type, context);
            fail();
        } catch (DuplicatePropertyException e) {
            // expected
        }
    }

    public void testNamesOnConstructor() throws Exception {
        PojoComponentType type = new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(Integer.class);
        processor.visitConstructor(ctor, type, context);
        assertNotNull(type.getProperties().get("myProp"));
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
        public Foo(@Property(name = "myProp", required = true)String prop) {

        }

        @org.osoa.sca.annotations.Constructor("myProp")
        public Foo(@Property Integer prop) {

        }

        @org.osoa.sca.annotations.Constructor()
        public Foo(@Property(name = "myProp1")String prop1, @Property(name = "myProp2")String prop2) {

        }

        @org.osoa.sca.annotations.Constructor()
        public Foo(@Property List prop) {

        }
    }

    private static class BadFoo {

        @org.osoa.sca.annotations.Constructor()
        public BadFoo(@Property(name = "myProp")String prop1, @Property(name = "myProp")String prop2) {

        }

        @org.osoa.sca.annotations.Constructor()
        public BadFoo(@Property String prop) {

        }

        @org.osoa.sca.annotations.Constructor("myProp")
        public BadFoo(@Property Integer prop, @Property Integer prop2) {

        }

        @org.osoa.sca.annotations.Constructor({"myRef", "myRef2"})
        public BadFoo(@Property List ref, @Property(name = "myOtherRef")List ref2) {

        }

    }

}
