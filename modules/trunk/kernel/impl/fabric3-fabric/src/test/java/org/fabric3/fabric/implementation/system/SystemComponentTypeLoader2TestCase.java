/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.implementation.system;

import java.util.ArrayList;
import java.util.Collection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.introspection.java.ClassWalker;
import org.fabric3.introspection.java.HeuristicProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.introspection.xml.LoaderException;

/**
 * @version $Rev$ $Date$
 */
public class SystemComponentTypeLoader2TestCase extends TestCase {
    private SystemComponentTypeLoaderImpl2 loader;
    private ClassWalker<SystemImplementation> classWalker;
    private IntrospectionContext context;
    private SystemImplementation impl;
    private Collection<HeuristicProcessor<SystemImplementation>> heuristics;
    private HeuristicProcessor<SystemImplementation> heuristic;
    private IMocksControl control;

    public void testSimple() throws LoaderException, IntrospectionException {
        impl.setImplementationClass(Simple.class.getName());

        classWalker.walk(impl, Simple.class, context);
        heuristic.applyHeuristics(impl, Simple.class, context);
        control.replay();
        loader.load(impl, context);

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
        impl = new SystemImplementation();

        context = EasyMock.createMock(IntrospectionContext.class);
        EasyMock.expect(context.getTargetClassLoader()).andStubReturn(getClass().getClassLoader());
        EasyMock.replay(context);

        control = EasyMock.createControl();
        classWalker = control.createMock(ClassWalker.class);
        heuristic = control.createMock(HeuristicProcessor.class);

        heuristics = new ArrayList<HeuristicProcessor<SystemImplementation>>();
        heuristics.add(heuristic);
        this.loader = new SystemComponentTypeLoaderImpl2(classWalker, heuristics);
    }
}
