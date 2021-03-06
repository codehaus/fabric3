/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.java.introspection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.implementation.java.model.JavaImplementation;
import org.fabric3.spi.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.introspection.java.HeuristicProcessor;

/**
 * @version $Rev$ $Date$
 */
public class JavaComponentTypeLoaderTestCase extends TestCase {

    private JavaImplementationProcessorImpl loader;
    private ClassVisitor<JavaImplementation> classVisitor;
    private IntrospectionContext context;
    private JavaImplementation impl;
    private HeuristicProcessor<JavaImplementation> heuristic;
    private IMocksControl control;

    public void testSimple() throws IntrospectionException {
        impl.setImplementationClass(Simple.class.getName());

        classVisitor.visit(EasyMock.same(impl), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        heuristic.applyHeuristics(EasyMock.same(impl), EasyMock.eq(Simple.class), EasyMock.isA(IntrospectionContext.class));
        control.replay();
        loader.introspect(impl, context);

        InjectingComponentType componentType = impl.getComponentType();
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
        helper.resolveTypeParameters(Simple.class, null);
        EasyMock.replay(helper);


        context = EasyMock.createNiceMock(IntrospectionContext.class);
        EasyMock.expect(context.getClassLoader()).andStubReturn(cl);
        EasyMock.replay(context);

        control = EasyMock.createControl();
        classVisitor = control.createMock(ClassVisitor.class);
        heuristic = control.createMock(HeuristicProcessor.class);

        this.loader = new JavaImplementationProcessorImpl(classVisitor, heuristic, helper);
    }
}
