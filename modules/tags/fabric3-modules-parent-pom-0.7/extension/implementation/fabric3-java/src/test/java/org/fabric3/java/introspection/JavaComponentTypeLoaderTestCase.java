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
 */
package org.fabric3.java.introspection;

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionException;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.java.ClassWalker;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
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
