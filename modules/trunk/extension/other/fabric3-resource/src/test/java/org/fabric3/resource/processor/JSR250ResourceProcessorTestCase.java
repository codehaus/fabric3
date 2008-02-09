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

import org.fabric3.pojo.processor.DuplicateResourceException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.spi.idl.InvalidServiceContractException;
import org.fabric3.spi.idl.java.JavaInterfaceProcessor;
import org.fabric3.spi.idl.java.JavaInterfaceProcessorRegistry;
import org.fabric3.spi.idl.java.JavaServiceContract;

/**
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class JSR250ResourceProcessorTestCase extends TestCase {

    PojoComponentType type;
    JSR250ResourceProcessor processor;
    
    public void setUp() {
        processor = new JSR250ResourceProcessor();
        type = new PojoComponentType(null);
        processor.setJavaInterfaceProcessorRegistry(new JavaInterfaceProcessorRegistry() {

            public void registerProcessor(JavaInterfaceProcessor processor) {
            }

            public void unregisterProcessor(JavaInterfaceProcessor processor) {
            }

            public JavaServiceContract introspect(Class<?> type) throws InvalidServiceContractException {
                return new JavaServiceContract(type);
            }

            public JavaServiceContract introspect(Class<?> type, Class<?> callback)
                    throws InvalidServiceContractException {
                return null;
            }
            
        });
    }

    public void testVisitField() throws Exception {
        Field field = JSR250ResourceProcessorTestCase.Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, null);
        SystemSourcedResource resource = (SystemSourcedResource) type.getResources().get("bar");
        assertNotNull(resource);
        assertFalse(resource.isOptional());
        assertEquals("", resource.getMappedName());

        field = JSR250ResourceProcessorTestCase.Foo.class.getDeclaredField("subBar");
        processor.visitField(field, type, null);
        resource = (SystemSourcedResource) type.getResources().get("someName");
        assertNotNull(resource);
        assertFalse(resource.isOptional());
        assertEquals("mapped",resource.getMappedName());
    }

    public void testVisitMethod() throws Exception {
        Method method = JSR250ResourceProcessorTestCase.Foo.class.getMethod("setBar", JSR250ResourceProcessorTestCase.Bar.class);
        processor.visitMethod(method, type, null);
        SystemSourcedResource resource = (SystemSourcedResource) type.getResources().get("bar");
        assertNotNull(resource);
        assertFalse(resource.isOptional());
        assertEquals("", resource.getMappedName());

        method = JSR250ResourceProcessorTestCase.Foo.class.getMethod("setSubBar", JSR250ResourceProcessorTestCase.Bar.class);
        processor.visitMethod(method, type, null);
        resource = (SystemSourcedResource) type.getResources().get("someName");
        assertNotNull(resource);
        assertFalse(resource.isOptional());
        assertEquals("mapped",resource.getMappedName());

    }

    public void testVisitBadMethod() throws Exception {
        Method method = JSR250ResourceProcessorTestCase.Foo.class.getMethod("setBadMethod");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
        method = JSR250ResourceProcessorTestCase.Foo.class.getMethod("setBadMethodType",Bar.class);
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        } 
        method = JSR250ResourceProcessorTestCase.Foo.class.getMethod("setBadMethodReturnType");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
    }
    
        public void testVisitBadField() throws Exception {
        Field field = JSR250ResourceProcessorTestCase.Foo.class.getDeclaredField("badField");
        try {
            processor.visitField(field, type, null);
            fail();
        } catch (IllegalResourceException e) {
            // expected
        }
    }


    public void testDuplicateResources() throws Exception {
        Field field = JSR250ResourceProcessorTestCase.Foo.class.getDeclaredField("bar");
        processor.visitField(field, type, null);
        try {
            processor.visitField(field, type, null);
            fail();
        } catch (DuplicateResourceException e) {
            //expected
        }
    }

    private class Foo {

        @javax.annotation.Resource
        protected Bar bar;

        @javax.annotation.Resource(name = "someName", mappedName = "mapped", type = SubBar.class, shareable=false)
        protected Bar subBar;

        @javax.annotation.Resource(type=Foo.class)
        protected Bar badField;

        @javax.annotation.Resource
        public void setBar(Bar bar) {
        }

        @javax.annotation.Resource(name = "someName", mappedName = "mapped", type = SubBar.class, shareable=false)
        public void setSubBar(Bar bar) {
        }

        
        @javax.annotation.Resource
        public void setBadMethod() {
        }
        
        @javax.annotation.Resource(type=Foo.class)
        public void setBadMethodType(Bar bar) {
        }
        
        @javax.annotation.Resource
        public String setBadMethodReturnType() {
            return null;
        }

    }
    
    private class SubFoo extends Foo {
        
    }

    private interface Bar {

    }
    
    private interface SubBar extends Bar {

    }
}
