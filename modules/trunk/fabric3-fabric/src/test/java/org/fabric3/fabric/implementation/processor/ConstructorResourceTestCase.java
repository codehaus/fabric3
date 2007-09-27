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

import org.fabric3.api.annotation.Resource;

import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;
import org.fabric3.fabric.idl.java.JavaInterfaceProcessorRegistryImpl;

/**
 * @version $Rev$ $Date$
 */
public class ConstructorResourceTestCase extends TestCase {

    ConstructorProcessor processor =
        new ConstructorProcessor(new ImplementationProcessorServiceImpl(new JavaInterfaceProcessorRegistryImpl()));

    public void testDummy() {
    }
    
    public void _testResource() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class);
        processor.visitConstructor(ctor, type, null);
        org.fabric3.pojo.scdl.Resource resource = type.getResources().get("myResource");
        assertFalse(resource.isOptional());
    }

    public void _testTwoResourcesSameType() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(String.class, String.class);
        processor.visitConstructor(ctor, type, null);
        assertNotNull(type.getResources().get("myResource1"));
        assertNotNull(type.getResources().get("myResource2"));
    }

    public void _testDuplicateResource() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<BadFoo> ctor = BadFoo.class.getConstructor(String.class, String.class);
        try {
            processor.visitConstructor(ctor, type, null);
            fail();
        } catch (DuplicateResourceException e) {
            // expected
        }
    }

    public void _testNoName() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(String.class);
        try {
            processor.visitConstructor(ctor, type, null);
            fail();
        } catch (InvalidResourceException e) {
            // expected
        }
    }

    public void _testNamesOnConstructor() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<Foo> ctor = Foo.class.getConstructor(Integer.class);
        processor.visitConstructor(ctor, type, null);
        assertNotNull(type.getResources().get("myResource"));
    }

    public void _testInvalidNumberOfNames() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(Integer.class, Integer.class);
        try {
            processor.visitConstructor(ctor, type, null);
            fail();
        } catch (InvalidResourceException e) {
            // expected
        }
    }

    public void _testNoMatchingNames() throws Exception {
        PojoComponentType type =
            new PojoComponentType(null);
        Constructor<ConstructorResourceTestCase.BadFoo> ctor =
            ConstructorResourceTestCase.BadFoo.class.getConstructor(List.class, List.class);
        try {
            processor.visitConstructor(ctor, type, null);
            fail();
        } catch (InvalidConstructorException e) {
            // expected
        }
    }

    private static class Foo {

        @org.osoa.sca.annotations.Constructor
        public Foo(@Resource(name = "myResource") String resource) {

        }

        @org.osoa.sca.annotations.Constructor("myResource")
        public Foo(@Resource Integer resource) {

        }

        @org.osoa.sca.annotations.Constructor
        public Foo(@Resource(name = "myResource1") String res1, @Resource(name = "myResource2") String res2) {

        }

        @org.osoa.sca.annotations.Constructor
        public Foo(@Resource List res) {

        }
    }

    private static class BadFoo {

        @org.osoa.sca.annotations.Constructor
        public BadFoo(@Resource(name = "myResource") String res1, @Resource(name = "myResource") String res2) {

        }

        @org.osoa.sca.annotations.Constructor
        public BadFoo(@Resource String res) {

        }

        @org.osoa.sca.annotations.Constructor("myProp")
        public BadFoo(@Resource Integer res, @Resource Integer res2) {

        }

        @org.osoa.sca.annotations.Constructor({"myRes", "myRes2"})
        public BadFoo(@Resource List res, @Resource(name = "myOtherRes") List res2) {

        }

    }

}
