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

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.osoa.sca.Conversation;

import org.fabric3.fabric.wire.jdk.ConversationImpl;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.component.TargetResolutionException;
import org.fabric3.spi.component.ExpirationPolicy;

/**
 * @version $Rev$ $Date$
 */
public class StatefulScopeContainerTestCase extends TestCase {
    private StatefulScopeContainer<Conversation> container;
    private IMocksControl control;
    private InstanceWrapperStore<Conversation> store;
    private Scope<Conversation> scope;
    private Conversation conversation;
    private WorkContext workContext;
    private URI groupId;
    private AtomicComponent<Object> component;
    private InstanceWrapper<Object> wrapper;

    public void testCorrectScope() {
        assertSame(scope, container.getScope());
    }

    public void testStoreIsNotifiedOfContextStartStop() throws GroupInitializationException {
        store.startContext(conversation);
        store.stopContext(conversation);
        control.replay();
        container.startContext(workContext, groupId);
        container.stopContext(workContext);
        control.verify();
    }

    public void testWrapperCreatedIfNotFound() throws TargetResolutionException, ObjectCreationException {
        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(null);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        wrapper.start();
        store.putWrapper(component, conversation, wrapper);
        control.replay();
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
        groupId = URI.create("groupId");
        conversation = new ConversationImpl("contextId");
        scope = new Scope<Conversation>("TESTING", Conversation.class);
        control = EasyMock.createControl();
        store = control.createMock(InstanceWrapperStore.class);
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
        container = new StatefulScopeContainer<Conversation>(scope, null, store) {

            public void startContext(WorkContext workContext, URI groupId) throws GroupInitializationException {
                super.startContext(workContext, conversation, groupId);
            }

            public void startContext(WorkContext workContext, URI groupId, ExpirationPolicy policy) throws GroupInitializationException {

            }

            public void stopContext(WorkContext workContext) {
                super.stopContext(conversation);
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
}
