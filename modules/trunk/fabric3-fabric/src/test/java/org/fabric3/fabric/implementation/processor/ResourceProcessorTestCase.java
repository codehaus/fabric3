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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.fabric3.pojo.processor.PojoComponentType;
import org.fabric3.pojo.processor.Resource;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class ResourceProcessorTestCase extends TestCase {

    PojoComponentType type;
    ResourceProcessor processor = new ResourceProcessor();

    public void testVisitField() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, null);
        Resource resource = type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertNull(resource.getMappedName());
        assertEquals(field.getType(), resource.getType());
    }

    public void testVisitMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar", Bar.class);
        processor.visitMethod(method, type, null);
        Resource resource = type.getResources().get("bar");
        assertFalse(resource.isOptional());
        assertNull(resource.getMappedName());
        assertEquals(method.getParameterTypes()[0], resource.getType());
    }

    public void testVisitNamedMethod() throws Exception {
        Method method = Foo.class.getMethod("setBar2", Bar.class);
        processor.visitMethod(method, type, null);
        Resource resource = type.getResources().get("someName");
        assertFalse(resource.isOptional());
        assertEquals("mapped", resource.getMappedName());
    }

    public void testVisitBadMethod() throws Exception {
        Method method = Foo.class.getMethod("setBad");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
    }

    public void testDuplicateResources() throws Exception {
        Field field = Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, null);
        try {
            processor.visitField(field, type, null);
            fail();
        } catch (DuplicateResourceException e) {
            //expected
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        type = new PojoComponentType(null);
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
