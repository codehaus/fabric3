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
import java.lang.annotation.ElementType;

import junit.framework.TestCase;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;

import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.InjectionSite;
import org.fabric3.scdl.MethodInjectionSite;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.FieldInjectionSite;
import org.fabric3.scdl.InjectableAttribute;

/**
 * @version $Rev$ $Date$
 */
public class ContextProcessorTestCase extends TestCase {
    private ContextProcessor processor;

    // FIXME: resurrect to test ComponentContext injection
/*
    public void testCompositeContextMethod() throws Exception {
        Method method = Foo.class.getMethod("setContext", ComponentContext.class);
        PojoComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>> type =
            new PojoComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>>();
        processor.visitMethod(composite, method, type, null);
        assertNotNull(type.getResources().get("context"));
    }
*/

    // FIXME: resurrect to test ComponentContext injection
/*
    public void testCompositeContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("context");
        PojoComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>> type =
            new PojoComponentType<JavaMappedService, JavaMappedReference, JavaMappedProperty<?>>();
        processor.visitField(composite, field, type, null);
        assertNotNull(type.getResources().get("context"));
    }
*/

    public void testRequestContextMethod() throws Exception {
        Method method = Foo.class.getMethod("setRequestContext", RequestContext.class);
        PojoComponentType type = new PojoComponentType(null);
        processor.visitMethod(method, type, null);
        InjectionSite requestContextMember = type.getInjectionSite(InjectableAttribute.REQUEST_CONTEXT);
        assertEquals(ElementType.METHOD, requestContextMember.getElementType());
        assertEquals(RequestContext.class.getName(), requestContextMember.getType());
        assertEquals(new Signature(method), ((MethodInjectionSite)requestContextMember).getSignature());
    }

    public void testRequestContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("requestContext");
        PojoComponentType type = new PojoComponentType(null);
        processor.visitField(field, type, null);
        InjectionSite requestContextMember = type.getInjectionSite(InjectableAttribute.REQUEST_CONTEXT);
        assertEquals(ElementType.FIELD, requestContextMember.getElementType());
        assertEquals(RequestContext.class.getName(), requestContextMember.getType());
        assertEquals(field.getName(), ((FieldInjectionSite)requestContextMember).getName());
    }

    public void testInvalidParamNum() throws Exception {
        Method method = Foo.class.getMethod("setContext", ComponentContext.class, String.class);
        PojoComponentType type =
            new PojoComponentType(null);
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalContextException e) {
            // expected
        }
    }

    public void testInvalidNoParams() throws Exception {
        Method method = Foo.class.getMethod("setContext");
        PojoComponentType type =
            new PojoComponentType(null);
        try {
            processor.visitMethod(method, type, null);
            fail();
        } catch (IllegalContextException e) {
            // expected
        }
    }

    public void testNoContext() throws Exception {
        Method method = Foo.class.getMethod("noContext", ComponentContext.class);
        PojoComponentType type =
            new PojoComponentType(null);
        processor.visitMethod(method, type, null);
        assertEquals(0, type.getResources().size());
    }

    public void testNoContextField() throws Exception {
        Field field = Foo.class.getDeclaredField("noContext");
        PojoComponentType type =
            new PojoComponentType(null);
        processor.visitField(field, type, null);
        assertEquals(0, type.getResources().size());
    }

    protected void setUp() throws Exception {
        super.setUp();
        processor = new ContextProcessor();
    }

    private class Foo {
        @Context
        protected ComponentContext context;

        @Context
        protected Object badContext;

        protected ComponentContext noContext;

        @Context
        protected RequestContext requestContext;

        @Context
        public void setContext(ComponentContext context) {

        }

        @Context
        public void setContext(String context) {

        }

        @Context
        public void setContext(ComponentContext context, String string) {

        }

        @Context
        public void setContext() {

        }

        public void noContext(ComponentContext context) {

        }

        @Context
        public void setRequestContext(RequestContext requestContext) {
            this.requestContext = requestContext;
        }
    }
}
