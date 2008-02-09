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
package org.fabric3.java;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IAnswer;

import org.fabric3.pojo.processor.IntrospectionRegistry;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.IntrospectionContext;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoaderTestCase extends TestCase {

    @SuppressWarnings("unchecked")
    public void testPojoComponentTypeCreatedForIntrospection() throws Exception {
        IntrospectionRegistry registry = EasyMock.createMock(IntrospectionRegistry.class);
        registry.introspect(
                (Class) EasyMock.isA(Object.class),
                EasyMock.isA(PojoComponentType.class),
                EasyMock.isA(IntrospectionContext.class));
        EasyMock.expectLastCall().andStubAnswer(new IAnswer() {
            public Object answer() throws Throwable {
                return null;
            }
        });
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        IntrospectionContext context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andStubReturn(cl);
        EasyMock.replay(registry, context);
        JavaComponentTypeLoaderImpl loader = new JavaComponentTypeLoaderImpl(registry);
        JavaImplementation implementation = new JavaImplementation();
        implementation.setImplementationClass(Object.class.getName());
        loader.loadByIntrospection(implementation, context);
        EasyMock.verify(registry, context);
    }

/*
    @SuppressWarnings("unchecked")
    public void testPojoComponentTypeCreatedForSideFileLoadAndReturned() throws Exception {
        LoaderRegistry registry = EasyMock.createMock(LoaderRegistry.class);
        registry.load(
                (URL) EasyMock.isNull(),
            EasyMock.eq(PojoComponentType.class),
            (IntrospectionContext) EasyMock.isNull());
        EasyMock.expectLastCall().andStubAnswer(new IAnswer() {
            public Object answer() throws Throwable {
                return EasyMock.getCurrentArguments()[0];
            }
        });
        EasyMock.replay(registry);
        JavaComponentTypeLoader loader = new JavaComponentTypeLoader(registry, null);
        assertEquals(PojoComponentType.class, loader.loadFromSidefile(null, null).getClass());
        EasyMock.verify(registry);
    }
*/


}
