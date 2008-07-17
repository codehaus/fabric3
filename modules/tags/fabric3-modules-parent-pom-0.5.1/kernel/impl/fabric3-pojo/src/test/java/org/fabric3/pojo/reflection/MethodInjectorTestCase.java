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
package org.fabric3.pojo.reflection;

import java.lang.reflect.Method;

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;

/**
 * @version $Rev$ $Date$
 */
public class MethodInjectorTestCase extends TestCase {
    private Method fooMethod;
    private Method privateMethod;
    private Method exceptionMethod;
    private ObjectFactory objectFactory;

    public void testIllegalArgument() throws Exception {
        EasyMock.expect(objectFactory.getInstance()).andReturn(new Object());
        EasyMock.replay(objectFactory);
        MethodInjector<Foo> injector = new MethodInjector<Foo>(fooMethod, objectFactory);
        try {
            injector.inject(new Foo());
            fail();
        } catch (ObjectCreationException e) {
            // expected
        }
    }

    public void testException() throws Exception {
        EasyMock.expect(objectFactory.getInstance()).andReturn("foo");
        EasyMock.replay(objectFactory);
        MethodInjector<Foo> injector = new MethodInjector<Foo>(exceptionMethod, objectFactory);
        try {
            injector.inject(new Foo());
            fail();
        } catch (ObjectCreationException e) {
            // expected
        }
    }

    protected void setUp() throws Exception {
        super.setUp();
        fooMethod = Foo.class.getMethod("foo", String.class);
        privateMethod = Foo.class.getDeclaredMethod("hidden", String.class);
        exceptionMethod = Foo.class.getDeclaredMethod("exception", String.class);
        objectFactory = EasyMock.createMock(ObjectFactory.class);
    }

    private class Foo {

        public void foo(String bar) {
        }

        private void hidden(String bar) {
        }

        public void exception(String bar) {
            throw new RuntimeException();
        }

    }
}
