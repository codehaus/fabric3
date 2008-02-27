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

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Property;
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.TypeMapping;

/**
 * @version $Rev$ $Date$
 */
public class PropertyProcessorTestCase extends TestCase {

    PojoComponentType type;
    PropertyProcessor processor;
    private IntrospectionContext context;

    public void testMethodAnnotation() throws Exception {
        processor.visitMethod(Foo.class.getMethod("setFoo", String.class), type, context);
        assertNotNull(type.getProperties().get("foo"));
    }

    public void testMethodRequired() throws Exception {
        processor.visitMethod(Foo.class.getMethod("setFooRequired", String.class), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("fooRequired");
        assertNotNull(prop);
        assertTrue(prop.isRequired());
    }

    public void testMethodName() throws Exception {
        processor.visitMethod(Foo.class.getMethod("setBarMethod", String.class), type, context);
        assertNotNull(type.getProperties().get("bar"));
    }

    public void testFieldAnnotation() throws Exception {
        processor.visitField(Foo.class.getDeclaredField("baz"), type, context);
        assertNotNull(type.getProperties().get("baz"));
    }

    public void testFieldRequired() throws Exception {
        processor.visitField(Foo.class.getDeclaredField("bazRequired"), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("bazRequired");
        assertNotNull(prop);
        assertTrue(prop.isRequired());
    }

    public void testFieldName() throws Exception {
        processor.visitField(Foo.class.getDeclaredField("bazField"), type, context);
        assertNotNull(type.getProperties().get("theBaz"));
    }

    public void testDuplicateFields() throws Exception {
        processor.visitField(Bar.class.getDeclaredField("dup"), type, context);
        try {
            processor.visitField(Bar.class.getDeclaredField("baz"), type, context);
            fail();
        } catch (DuplicatePropertyException e) {
            // expected
        }
    }

    public void testDuplicateMethods() throws Exception {
        processor.visitMethod(Bar.class.getMethod("dupMethod", String.class), type, context);
        try {
            processor.visitMethod(Bar.class.getMethod("dupSomeMethod", String.class), type, context);
            fail();
        } catch (DuplicatePropertyException e) {
            // expected
        }
    }

    public void testInvalidProperty() throws Exception {
        try {
            processor.visitMethod(Bar.class.getMethod("badMethod"), type, context);
            fail();
        } catch (IllegalPropertyException e) {
            // expected
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        type = new PojoComponentType(null);
        processor = new PropertyProcessor(new DefaultIntrospectionHelper());

        context = EasyMock.createMock(IntrospectionContext.class);
        TypeMapping typeMapping = new TypeMapping();
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);


    }

    private class Foo {

        @Property
        protected String baz;
        @Property(required = true)
        protected String bazRequired;
        @Property(name = "theBaz")
        protected String bazField;

        @Property
        public void setFoo(String string) {
        }

        @Property(required = true)
        public void setFooRequired(String string) {
        }

        @Property(name = "bar")
        public void setBarMethod(String string) {
        }

    }

    private class Bar {

        @Property
        protected String dup;

        @Property(name = "dup")
        protected String baz;

        @Property
        public void dupMethod(String s) {
        }

        @Property(name = "dupMethod")
        public void dupSomeMethod(String s) {
        }

        @Property
        public void badMethod() {
        }

    }

    private class Multiple {
        @Property
        protected List<String> refs1;

        @Property
        protected String[] refs2;

        @Property
        public void setRefs3(String[] refs) {
        }

        @Property
        public void setRefs4(Collection<String> refs) {
        }

    }

    public void testMultiplicityCollection() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs1"), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("refs1");
        assertNotNull(prop);
        assertTrue(prop.isMany());
    }

    public void testMultiplicityArray() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs2"), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("refs2");
        assertNotNull(prop);
        assertTrue(prop.isMany());
    }

    public void testMultiplicityArrayMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs3", String[].class), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("refs3");
        assertNotNull(prop);
        assertTrue(prop.isMany());
    }

    public void testMultiplicityCollectionMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs4", Collection.class), type, context);
        org.fabric3.scdl.Property prop = type.getProperties().get("refs4");
        assertNotNull(prop);
        assertTrue(prop.isMany());
    }

}
