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
package org.fabric3.fabric.component.scope;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.ExpirationPolicy;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $Rev$ $Date$
 */
public class StatefulScopeContainerTestCase extends TestCase {
    private StatefulScopeContainer<MockId> container;
    private IMocksControl control;
    private InstanceWrapperStore<MockId> store;
    private Scope<MockId> scope;
    private MockId conversation;
    private WorkContext workContext;
    private AtomicComponent<Object> component;
    private InstanceWrapper<Object> wrapper;

    public void testCorrectScope() {
        assertSame(scope, container.getScope());
    }

    public void testStoreIsNotifiedOfContextStartStop() throws GroupInitializationException {
        store.startContext(conversation);
        store.stopContext(conversation);
        control.replay();
        container.startContext(workContext);
        container.stopContext(workContext);
        control.verify();
    }

    public void testWrapperCreatedIfNotFound() throws Exception {
        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(null);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        wrapper.start();
        store.putWrapper(component, conversation, wrapper);
        store.startContext(EasyMock.eq(conversation));
        control.replay();
        container.startContext(workContext);
        assertSame(wrapper, container.getWrapper(component, workContext));
        control.verify();
    }

    public void testWrapperReturnedIfFound() throws TargetResolutionException {
        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(wrapper);
        control.replay();
        assertSame(wrapper, container.getWrapper(component, workContext));
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        conversation = new MockId("contextId");
        scope = new Scope<MockId>("TESTING", MockId.class);
        control = EasyMock.createControl();
        store = control.createMock(InstanceWrapperStore.class);
        workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame(conversation));
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
        container = new StatefulScopeContainer<MockId>(scope, null, store) {

            public void startContext(WorkContext workContext) throws GroupInitializationException {
                super.startContext(workContext, conversation);
            }

            public void startContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {

            }

            public void joinContext(WorkContext workContext) throws GroupInitializationException {

            }

            public void joinContext(WorkContext workContext, ExpirationPolicy policy) throws GroupInitializationException {

            }

            public void stopContext(WorkContext workContext) {
                super.stopContext(workContext, conversation);
            }

            public <T> InstanceWrapper<T> getWrapper(AtomicComponent<T> component, WorkContext workContext) throws TargetResolutionException {
                return super.getWrapper(component, workContext, conversation, true);
            }

            public void reinject() {
            }

            public void addObjectFactory(AtomicComponent<?> component, ObjectFactory<?> factory, String referenceName, Object key) {
            }

        };
    }

    private class MockId {
        private String id;

        public MockId(String id) {
            this.id = id;
        }

        public Object getId() {
            return id;
        }

    }
}
