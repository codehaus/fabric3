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
package org.fabric3.resource.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.fabric3.introspection.impl.contract.DefaultContractProcessor;
import org.fabric3.introspection.impl.DefaultIntrospectionHelper;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.introspection.java.TypeMapping;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.pojo.processor.DuplicateResourceException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.resource.model.SystemSourcedResource;

import org.easymock.EasyMock;

/**
 * @version $Rev$ $Date$
 */
public class ResourceProcessorTestCase extends TestCase {

    PojoComponentType type;
    ResourceProcessor processor;
    private IntrospectionContext context;

    protected void setUp() throws Exception {
        super.setUp();
        IntrospectionHelper helper = new DefaultIntrospectionHelper();
        processor = new ResourceProcessor(new DefaultContractProcessor(helper));
        type = new PojoComponentType(null);

        TypeMapping typeMapping = new TypeMapping();
        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTypeMapping()).andStubReturn(typeMapping);
        EasyMock.replay(context);
    }

    public void testVisitField() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, context);
        SystemSourcedResource resource = (SystemSourcedResource) type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertEquals("", resource.getMappedName());
    }

    public void testVisitMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar", Bar.class);
        processor.visitMethod(method, type, context);
        SystemSourcedResource resource = (SystemSourcedResource) type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertEquals("", resource.getMappedName());
    }

    public void testVisitNamedMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar2", Bar.class);
        processor.visitMethod(method, type, context);
        SystemSourcedResource resource = (SystemSourcedResource) type.getResources().get("someName");
        assertFalse(resource.isOptional());
        assertEquals("mapped", resource.getMappedName());
    }

    public void testVisitBadMethod() throws Exception {
        Method method = Foo.class.getMethod("setBad");
        try {
            processor.visitMethod(method, type, context);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
    }

    public void testDuplicateResources() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, context);
        try {
            processor.visitField(field, type, context);
            fail();
        } catch (DuplicateResourceException e) {
            //expected
        }
    }

    private class Foo {

        @org.fabric3.api.annotation.Resource
        protected Bar bar;

        @org.fabric3.api.annotation.Resource(optional = true)
        protected Bar barNotRequired;

        @org.fabric3.api.annotation.Resource
        public void setBar(Bar bar) {
        }

        @org.fabric3.api.annotation.Resource(name = "someName", mappedName = "mapped")
        public void setBar2(Bar bar) {
        }

        @org.fabric3.api.annotation.Resource
        public void setBad() {
        }

    }

    private interface Bar {

    }
}
