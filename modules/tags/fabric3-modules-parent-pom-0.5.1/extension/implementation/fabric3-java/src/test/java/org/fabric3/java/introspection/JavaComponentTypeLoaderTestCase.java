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
package org.fabric3.java.introspection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoaderTestCase extends TestCase {

    private JavaImplementationProcessorImpl loader;
    private ClassWalker<JavaImplementation> classWalker;
    private IntrospectionContext context;
    private JavaImplementation impl;
    private HeuristicProcessor<JavaImplementation> heuristic;
    private IMocksControl control;

    public void testSimple() throws IntrospectionException {
        impl.setImplementationClass(Simple.class.getName());

        classWalker.walk(EasyMock.same(impl), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        heuristic.applyHeuristics(EasyMock.same(impl), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        control.replay();
        loader.introspect(impl, context);

        PojoComponentType componentType = impl.getComponentType();
        assertNotNull(componentType);
        assertEquals(Simple.class.getName(), componentType.getImplClass());
        control.verify();
    }

    private static class Simple {
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        ClassLoader cl = getClass().getClassLoader();
        impl = new JavaImplementation();
        IntrospectionHelper helper = EasyMock.createNiceMock(IntrospectionHelper.class);
        EasyMock.expect(helper.loadClass(Simple.class.getName(), cl)).andStubReturn(Simple.class);
        EasyMock.expect(helper.mapTypeParameters(Simple.class)).andStubReturn(null);
        EasyMock.replay(helper);


        context = EasyMock.createNiceMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andStubReturn(cl);
        EasyMock.replay(context);

        control = EasyMock.createControl();
        classWalker = control.createMock(ClassWalker.class);
        heuristic = control.createMock(HeuristicProcessor.class);

        this.loader = new JavaImplementationProcessorImpl(classWalker, heuristic, helper);
    }
}
