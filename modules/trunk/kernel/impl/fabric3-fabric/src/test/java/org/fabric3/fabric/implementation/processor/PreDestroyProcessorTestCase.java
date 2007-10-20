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

import javax.annotation.PreDestroy;

import junit.framework.TestCase;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev: 751 $ $Date: 2007-08-16 14:50:14 -0500 (Thu, 16 Aug 2007) $
 */
public class PreDestroyProcessorTestCase extends TestCase {

    public void testDestroy() throws Exception {
        PreDestroyProcessor processor = new PreDestroyProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = Foo.class.getMethod("destroy");
        processor.visitMethod(method, type, null);
        assertNotNull(type.getDestroyMethod());
    }

    public void testBadDestroy() throws Exception {
        PreDestroyProcessor processor = new PreDestroyProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = Bar.class.getMethod("badDestroy", String.class);
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalDestructorException e) {
            // expected
        }
        method = Bar.class.getMethod("badDestroy2");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalDestructorException e) {
            // expected
        }
         method = Bar.class.getMethod("badDestroy3");
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalDestructorException e) {
            // expected
        }
    }

    public void testTwoDestroy() throws Exception {
        PreDestroyProcessor processor = new PreDestroyProcessor();
        PojoComponentType type =
            new PojoComponentType(null);
        Method method = Bar.class.getMethod("destroy");
        Method method2 = Bar.class.getMethod("destroy2");
        processor.visitMethod(method, type, null);
        try {
            processor.visitMethod(method2, type, null);
            fail();
        } catch (DuplicateDestructorException e) {
            // expected
        }
    }


    private class Foo {

        @PreDestroy
        public void destroy() {
        }
    }


    private static class Bar {

        @PreDestroy
        public void destroy() {
        }

        @PreDestroy
        public void destroy2() {
        }

        @PreDestroy
        public void badDestroy(String foo) {
        }
        
        @PreDestroy
        public static void badDestroy2() {
        }
        
        @PreDestroy
        public String badDestroy3() {
            return null;
        }
        
        
     }
}
