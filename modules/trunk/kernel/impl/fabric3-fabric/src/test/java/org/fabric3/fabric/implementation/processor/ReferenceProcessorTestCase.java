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
import org.osoa.sca.annotations.Reference;
import org.easymock.EasyMock;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Multiplicity;
import org.fabric3.scdl.ServiceContract;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class ReferenceProcessorTestCase extends TestCase {

    private PojoComponentType type;
    private ReferenceProcessor processor;
    private IntrospectionContext context;

    public void testMethodAnnotation() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Foo.class.getMethod("setFoo", Ref.class), type, context);
        ReferenceDefinition reference = type.getReferences().get("foo");
        assertNotNull(reference);
        ServiceContract contract = reference.getServiceContract();
        assertEquals(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals("Ref", contract.getInterfaceName());
    }

    public void testMethodRequired() throws Exception {
        processor.visitMethod(
                ReferenceProcessorTestCase.Foo.class.getMethod("setFooRequired", Ref.class),
                type,
                context);
        ReferenceDefinition reference = type.getReferences().get("fooRequired");
        assertNotNull(reference);
        assertTrue(reference.isRequired());
    }

    public void testMethodName() throws Exception {
        processor.visitMethod(
                ReferenceProcessorTestCase.Foo.class.getMethod("setBarMethod", Ref.class),
                type,
                context);
        assertNotNull(type.getReferences().get("bar"));
    }

    public void testFieldAnnotation() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("baz"), type, context);
        ReferenceDefinition reference = type.getReferences().get("baz");
        assertNotNull(reference);
        ServiceContract contract = reference.getServiceContract();
        assertEquals(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals("Ref", contract.getInterfaceName());
    }

    public void testFieldRequired() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("bazRequired"), type, context);
        ReferenceDefinition prop = type.getReferences().get("bazRequired");
        assertNotNull(prop);
        assertTrue(prop.isRequired());
    }

    public void testFieldName() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Foo.class.getDeclaredField("bazField"), type, context);
        assertNotNull(type.getReferences().get("theBaz"));
    }

    public void testDuplicateFields() throws Exception {
        processor.visitField(ReferenceProcessorTestCase.Bar.class.getDeclaredField("dup"), type, context);
        try {
            processor.visitField(ReferenceProcessorTestCase.Bar.class.getDeclaredField("baz"), type, context);
            fail();
        } catch (DuplicateReferenceException e) {
            // expected
        }
    }

    public void testDuplicateMethods() throws Exception {
        processor.visitMethod(ReferenceProcessorTestCase.Bar.class.getMethod("dupMethod", Ref.class), type, context);
        try {
            processor.visitMethod(
                    ReferenceProcessorTestCase.Bar.class.getMethod("dupSomeMethod", Ref.class),
                    type,
                    null);
            fail();
        } catch (DuplicateReferenceException e) {
            // expected
        }
    }

    public void testInvalidProperty() throws Exception {
        try {
            processor.visitMethod(ReferenceProcessorTestCase.Bar.class.getMethod("badMethod"), type, context);
            fail();
        } catch (IllegalReferenceException e) {
            // expected
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        type = new PojoComponentType(null);
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new ReferenceProcessor(helper, new DefaultContractProcessor(helper));

        TypeMapping typeMapping = new TypeMapping();
        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);

    }

    private interface Ref {
    }

    private class Foo {

        @Reference
        protected Ref baz;
        @Reference(required = true)
        protected Ref bazRequired;
        @Reference(name = "theBaz")
        protected Ref bazField;

        @Reference
        public void setFoo(Ref ref) {
        }

        @Reference(required = true)
        public void setFooRequired(Ref ref) {
        }

        @Reference(name = "bar")
        public void setBarMethod(Ref ref) {
        }

    }

    private class Bar {

        @Reference
        protected Ref dup;

        @Reference(name = "dup")
        protected Ref baz;

        @Reference
        public void dupMethod(Ref s) {
        }

        @Reference(name = "dupMethod")
        public void dupSomeMethod(Ref s) {
        }

        @Reference
        public void badMethod() {
        }

    }

    private class Multiple {
        @Reference(required = true)
        protected List<Ref> refs1;

        @Reference(required = false)
        protected Ref[] refs2;

        @Reference(required = true)
        public void setRefs3(Ref[] refs) {
        }

        @Reference(required = false)
        public void setRefs4(Collection<Ref> refs) {
        }

    }

    public void testMultiplicity1ToN() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs1"), type, context);
        ReferenceDefinition prop = type.getReferences().get("refs1");
        assertNotNull(prop);
        ServiceContract contract = prop.getServiceContract();
        assertSame(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals(Multiplicity.ONE_N, prop.getMultiplicity());
        assertTrue(prop.isRequired());
    }

    public void testMultiplicityTo0ToN() throws Exception {
        processor.visitField(Multiple.class.getDeclaredField("refs2"), type, context);
        ReferenceDefinition prop = type.getReferences().get("refs2");
        assertNotNull(prop);
        ServiceContract contract = prop.getServiceContract();
        assertSame(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals(Multiplicity.ZERO_N, prop.getMultiplicity());
        assertFalse(prop.isRequired());
    }

    public void testMultiplicity1ToNMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs3", Ref[].class), type, context);
        ReferenceDefinition prop = type.getReferences().get("refs3");
        assertNotNull(prop);
        ServiceContract contract = prop.getServiceContract();
        assertSame(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals(Multiplicity.ONE_N, prop.getMultiplicity());
        assertTrue(prop.isRequired());
    }

    public void testMultiplicity0ToNMethod() throws Exception {
        processor.visitMethod(Multiple.class.getMethod("setRefs4", Collection.class), type, context);
        ReferenceDefinition prop = type.getReferences().get("refs4");
        assertNotNull(prop);
        ServiceContract contract = prop.getServiceContract();
        assertSame(Ref.class.getName(), contract.getQualifiedInterfaceName());
        assertEquals(Multiplicity.ZERO_N, prop.getMultiplicity());
        assertFalse(prop.isRequired());
    }

}
