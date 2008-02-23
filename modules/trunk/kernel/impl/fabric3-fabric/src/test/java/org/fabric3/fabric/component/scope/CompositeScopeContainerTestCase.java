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
package org.fabric3.fabric.component.scope;

import java.net.URI;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.component.CallFrame;

/**
 * @version $$Rev$$ $$Date$$
 */
public class CompositeScopeContainerTestCase<T> extends TestCase {
    protected IMocksControl control;
    protected ScopeContainer<URI> scopeContainer;
    protected URI groupId;
    protected URI contextId;
    protected AtomicComponent<T> component;
    protected InstanceWrapper<T> wrapper;
    private WorkContext workContext;

    public void testCorrectScope() {
        assertEquals(Scope.COMPOSITE, scopeContainer.getScope());
    }

    public void testWrapperCreation() throws Exception {

        EasyMock.expect(component.isEagerInit()).andStubReturn(false);
        CallFrame frame = new CallFrame(contextId);
        EasyMock.expect(workContext.peekCallFrame()).andStubReturn(frame);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start();
        EasyMock.expect(component.getGroupId()).andStubReturn(contextId);
        control.replay();
        scopeContainer.register(component);
        scopeContainer.startContext(workContext, groupId);
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        contextId = URI.create("compositeId");
        groupId = URI.create("groupId");
        control = EasyMock.createStrictControl();
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
        scopeContainer = new CompositeScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));
        scopeContainer.start();
    }
}
