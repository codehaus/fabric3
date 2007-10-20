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

import java.lang.reflect.Method;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.fabric3.pojo.scdl.PojoComponentType;

import junit.framework.TestCase;

/**
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class PostConstructProcessorTestCase extends TestCase {

    public void testInit() throws Exception {
        PostConstructProcessor processor = new PostConstructProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = PostConstructProcessorTestCase.Foo.class.getMethod("init");
        processor.visitMethod(method, type, null);
        assertNotNull(type.getInitMethod());
        assertEquals(0, type.getInitLevel());
    }

    public void testBadInit() throws Exception {
        PostConstructProcessor processor = new PostConstructProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = PostConstructProcessorTestCase.Bar.class.getMethod("badInit", String.class);
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalInitException e) {
            // expected
        }
        method = PostConstructProcessorTestCase.Bar.class.getMethod("badInit2");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalInitException e) {
            // expected
        }
        method = PostConstructProcessorTestCase.Bar.class.getMethod("badInit3");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalInitException e) {
            // expected
        }
    }

    public void testTwoInit() throws Exception {
        PostConstructProcessor processor = new PostConstructProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = PostConstructProcessorTestCase.Bar.class.getMethod("init");
        Method method2 = PostConstructProcessorTestCase.Bar.class.getMethod("init2");
        processor.visitMethod(method, type, null);
        try {
            processor.visitMethod(method2, type, null);
            fail();
        } catch (DuplicateInitException e) {
            // expected
        }
    }


    private class Foo {
        @PostConstruct
        public void init() {
        }
    }


    private static class Bar {
        @PostConstruct
        public void init() {
        }

        @PostConstruct
        public void init2() {
        }

        @PostConstruct
        public void badInit(String foo) {
        }
        
        @PostConstruct
        public static void badInit2() {
        }
        
        @PostConstruct
        public String badInit3() {
            return null;
        }

    }
}
