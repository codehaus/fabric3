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

import junit.framework.TestCase;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;

import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.host.monitor.MonitorFactory;

/**
 * Unit tests for the composite scope container
 *
 * @version $Rev$ $Date$
 */
public class StatelessScopeContainerTestCase<T> extends TestCase {
    private StatelessScopeContainer scopeContainer;
    private IMocksControl control;
    private AtomicComponent<T> component;
    private InstanceWrapper<T> wrapper;
    private WorkContext workContext;

    public void testCorrectScope() {
        assertEquals(Scope.STATELESS, scopeContainer.getScope());
    }

    public void testInstanceCreation() throws Exception {
        @SuppressWarnings("unchecked")
        InstanceWrapper<T> wrapper2 = control.createMock(InstanceWrapper.class);

        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        wrapper.start();
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper2);
        wrapper2.start();
        control.replay();

        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        assertSame(wrapper2, scopeContainer.getWrapper(component, workContext));
        control.verify();
    }

    public void testReturnWrapper() throws Exception {
        wrapper.stop();
        control.replay();
        scopeContainer.returnWrapper(component, workContext, wrapper);
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        MonitorFactory mock = EasyMock.createNiceMock(MonitorFactory.class);
        scopeContainer = new StatelessScopeContainer(mock);

        control = EasyMock.createStrictControl();
        workContext = control.createMock(WorkContext.class);
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
    }
}
