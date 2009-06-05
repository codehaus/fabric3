/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.introspection.impl.annotation;

import java.lang.reflect.Method;
import java.net.URI;
import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Init;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;

@SuppressWarnings("unchecked")
public class OASISInitProcessorTestCase extends TestCase {

    public void testInvalidStringReturnInit() throws Exception {
        TestInvalidInitClass componentToProcess = new TestInvalidInitClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        OASISInitProcessor<Implementation<? extends InjectingComponentType>> processor =
                new OASISInitProcessor<Implementation<? extends InjectingComponentType>>();
        IntrospectionContext context = new DefaultIntrospectionContext((URI) null, null, null);
        processor.visitMethod(annotation, TestInvalidInitClass.class.getDeclaredMethod("init"), new TestImplementation(), context);
        assertEquals(1, context.getErrors().size());
        assertTrue(context.getErrors().get(0) instanceof InvalidMethod);
    }

    public void testInit() throws Exception {
        TestClass componentToProcess = new TestClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        OASISInitProcessor<Implementation<? extends InjectingComponentType>> processor =
                new OASISInitProcessor<Implementation<? extends InjectingComponentType>>();
        IntrospectionContext context = new DefaultIntrospectionContext((URI) null, null, null);
        TestImplementation impl = new TestImplementation();
        InjectingComponentType componentType = new InjectingComponentType() {

        };
        impl.setComponentType(componentType);
        Method method = TestClass.class.getDeclaredMethod("init");
        processor.visitMethod(annotation, method, impl, context);
        assertEquals(0, context.getWarnings().size());
        assertEquals(method, impl.getComponentType().getInitMethod().getMethod(TestClass.class));
    }

    public void testPrivateInit() throws Exception {
        TestPrivateInitClass componentToProcess = new TestPrivateInitClass();
        Init annotation = componentToProcess.getClass().getAnnotation(Init.class);
        OASISInitProcessor<Implementation<? extends InjectingComponentType>> processor =
                new OASISInitProcessor<Implementation<? extends InjectingComponentType>>();
        IntrospectionContext context = new DefaultIntrospectionContext((URI) null, null, null);
        TestImplementation impl = new TestImplementation();
        InjectingComponentType componentType = new InjectingComponentType() {

        };
        impl.setComponentType(componentType);
        Method method = TestClass.class.getDeclaredMethod("init");
        processor.visitMethod(annotation, method, impl, context);
        assertEquals(0, context.getWarnings().size());
        assertEquals(method, impl.getComponentType().getInitMethod().getMethod(TestClass.class));
    }


    public static class TestClass {
        @Init
        public void init() {

        }
    }

    public static class TestPrivateInitClass {
        @Init
        private void init() {

        }
    }

    public static class TestInvalidInitClass {
        @Init
        public String init() {
            return "test";
        }
    }

    public static class TestImplementation extends Implementation<InjectingComponentType> {
        private static final long serialVersionUID = 2759280710238779821L;

        public QName getType() {
            return null;
        }

    }

}