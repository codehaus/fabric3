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
 * --- Original Apache License ---
 *
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
package org.fabric3.fabric.component.scope;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.osoa.sca.Conversation;

import org.fabric3.pojo.ConversationImpl;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.GroupInitializationException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.InstanceWrapperStore;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.ConversationContext;

/**
 * @version $Rev$ $Date$
 */
public class ConversationalScopeContainerTestCase extends TestCase {
    private ConversationalScopeContainer container;
    private IMocksControl control;
    private InstanceWrapperStore<Conversation> store;
    private Conversation conversation;
    private WorkContext workContext;
    private AtomicComponent<Object> component;
    private InstanceWrapper<Object> wrapper;

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

    public void testWrapperReturnedIfFound() throws InstanceLifecycleException {
        EasyMock.expect(store.getWrapper(component, conversation)).andReturn(wrapper);
        control.replay();
        assertSame(wrapper, container.getWrapper(component, workContext));
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        control = EasyMock.createControl();
        store = control.createMock(InstanceWrapperStore.class);
        container = new ConversationalScopeContainer(null, store);
        conversation = new ConversationImpl("contextId", container);
        workContext = new WorkContext();
        CallFrame frame = new CallFrame(null, null, conversation, ConversationContext.NEW);
        workContext.addCallFrame(frame);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
    }

}
