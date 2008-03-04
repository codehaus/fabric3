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
import java.net.URI;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationRuntimeException;
import org.fabric3.spi.wire.Message;
import org.fabric3.scdl.Scope;

public class InvokerInterceptorBasicTestCase extends TestCase {
    private TestBean bean;
    private Method echoMethod;
    private Method arrayMethod;
    private Method nullParamMethod;
    private Method primitiveMethod;
    private Method checkedMethod;
    private Method runtimeMethod;

    private IMocksControl control;
    private WorkContext workContext;
    private ScopeContainer<URI> scopeContainer;
    private InstanceWrapper<TestBean> wrapper;
    private AtomicComponent<TestBean> component;
    private Message message;

    public void testObjectInvoke() throws Throwable {
        String value = "foo";
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(echoMethod, false, false,component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testPrimitiveInvoke() throws Throwable {
        Integer value = 1;
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(primitiveMethod, false,false, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testArrayInvoke() throws Throwable {
        String[] value = new String[]{"foo", "bar"};
        mockCall(new Object[]{value}, value);
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(arrayMethod, false,false, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testEmptyInvoke() throws Throwable {
        mockCall(new Object[]{}, "foo");
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(nullParamMethod, false,false, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testNullInvoke() throws Throwable {
        mockCall(null, "foo");
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(nullParamMethod, false, false,component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeCheckedException() throws Throwable {
        mockFaultCall(null, TestException.class);
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(checkedMethod, false,false, component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testInvokeRuntimeException() throws Throwable {
        mockFaultCall(null, TestRuntimeException.class);
        control.replay();
        InvokerInterceptor<TestBean, URI> invoker =
                new InvokerInterceptor<TestBean, URI>(runtimeMethod, false, false,component, scopeContainer);
        Message ret = invoker.invoke(message);
        assertSame(ret, message);
        control.verify();
    }

    public void testFailureGettingWrapperThrowsException() {
        EasyMock.expect(scopeContainer.getScope()).andReturn(Scope.COMPOSITE);
        TargetResolutionException ex = new TargetResolutionException(null);
        EasyMock.expect(message.getBody()).andReturn(null);
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        EasyMock.expect(workContext.peekCallFrame()).andReturn(new CallFrame());
        try {
            EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andThrow(ex);
        } catch (TargetResolutionException e) {
            throw new AssertionError();
        }
        control.replay();
        try {
            InvokerInterceptor<TestBean, URI> invoker =
                    new InvokerInterceptor<TestBean, URI>(echoMethod, false, false,component, scopeContainer);
            invoker.invoke(message);
            fail();
        } catch (InvocationRuntimeException e) {
            assertSame(ex, e.getCause());
            control.verify();
        }
    }

    private void mockCall(Object value, Object body) throws Exception {
        EasyMock.expect(scopeContainer.getScope()).andReturn(Scope.COMPOSITE);
        EasyMock.expect(message.getBody()).andReturn(value);
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        EasyMock.expect(workContext.peekCallFrame()).andReturn(new CallFrame());
        EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.getInstance()).andReturn(bean);
        message.setBody(body);
        scopeContainer.returnWrapper(component, workContext, wrapper);
    }

    private void mockFaultCall(Object value, Class<? extends Exception> fault) throws Exception {
        EasyMock.expect(scopeContainer.getScope()).andReturn(Scope.COMPOSITE);
        EasyMock.expect(message.getBody()).andReturn(value);
        EasyMock.expect(message.getWorkContext()).andReturn(workContext);
        EasyMock.expect(workContext.peekCallFrame()).andReturn(new CallFrame());
        EasyMock.expect(scopeContainer.getWrapper(component, workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.getInstance()).andReturn(bean);
        message.setBodyWithFault(EasyMock.isA(fault));
        scopeContainer.returnWrapper(component, workContext, wrapper);
    }

    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        bean = new TestBean();
        echoMethod = TestBean.class.getDeclaredMethod("echo", String.class);
        arrayMethod = TestBean.class.getDeclaredMethod("arrayEcho", String[].class);
        nullParamMethod = TestBean.class.getDeclaredMethod("nullParam");
        primitiveMethod = TestBean.class.getDeclaredMethod("primitiveEcho", Integer.TYPE);
        checkedMethod = TestBean.class.getDeclaredMethod("checkedException");
        runtimeMethod = TestBean.class.getDeclaredMethod("runtimeException");
        assertNotNull(echoMethod);
        assertNotNull(checkedMethod);
        assertNotNull(runtimeMethod);

        control = EasyMock.createStrictControl();
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        scopeContainer = control.createMock(ScopeContainer.class);
        wrapper = control.createMock(InstanceWrapper.class);
        message = control.createMock(Message.class);
    }

    private class TestBean {

        public String echo(String msg) throws Exception {
            assertEquals("foo", msg);
            return msg;
        }

        public String[] arrayEcho(String[] msg) throws Exception {
            assertNotNull(msg);
            assertEquals(2, msg.length);
            assertEquals("foo", msg[0]);
            assertEquals("bar", msg[1]);
            return msg;
        }

        public String nullParam() throws Exception {
            return "foo";
        }

        public int primitiveEcho(int i) throws Exception {
            return i;
        }

        public void checkedException() throws TestException {
            throw new TestException();
        }

        public void runtimeException() throws TestRuntimeException {
            throw new TestRuntimeException();
        }
    }

    public static class TestException extends Exception {
    }

    public static class TestRuntimeException extends RuntimeException {
    }
}
