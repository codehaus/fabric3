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

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.java.IntrospectionHelper;
import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoaderTestCase extends TestCase {

    private JavaComponentTypeLoaderImpl loader;
    private IntrospectionContext context;
    private Introspector introspector;
    private IntrospectionHelper helper;

    public void testPojoComponentTypeCreatedForIntrospection() throws Exception {
        introspector.introspect(EasyMock.eq(Object.class), EasyMock.isA(PojoComponentType.class), EasyMock.same(context));
        EasyMock.replay(introspector, helper, context);

        PojoComponentType componentType = loader.loadByIntrospection(Object.class, context);
        assertEquals("java.lang.Object", componentType.getImplClass());
        EasyMock.verify(introspector, helper, context);
    }

    protected void setUp() throws Exception {
        super.setUp();

        context = EasyMock.createMock(IntrospectionContext.class);
        introspector = EasyMock.createMock(Introspector.class);
        helper = EasyMock.createMock(IntrospectionHelper.class);
        loader = new JavaComponentTypeLoaderImpl(introspector, helper);
    }
}
