/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
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

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.component.AtomicComponent;
import org.fabric3.spi.component.InstanceWrapper;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContext;

/**
 * @version $$Rev$$ $$Date$$
 */
public class CompositeScopeContainerTestCase<T> extends TestCase {
    protected IMocksControl control;
    protected ScopeContainer<QName> scopeContainer;
    protected QName deployable;
    protected AtomicComponent<T> component;
    protected InstanceWrapper<T> wrapper;
    private WorkContext workContext;

    public void testCorrectScope() {
        assertEquals(Scope.COMPOSITE, scopeContainer.getScope());
    }

    public void testWrapperCreation() throws Exception {

        EasyMock.expect(component.isEagerInit()).andStubReturn(false);
        EasyMock.expect(component.createInstanceWrapper(workContext)).andReturn(wrapper);
        EasyMock.expect(wrapper.isStarted()).andReturn(false);
        wrapper.start(EasyMock.isA(WorkContext.class));
        EasyMock.expect(component.getDeployable()).andStubReturn(deployable);
        control.replay();
        scopeContainer.register(component);
        scopeContainer.startContext(workContext);
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        assertSame(wrapper, scopeContainer.getWrapper(component, workContext));
        control.verify();
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        deployable = new QName("deployable");
        control = EasyMock.createStrictControl();
        workContext = new WorkContext();
        workContext.addCallFrame(new CallFrame(deployable));
        component = control.createMock(AtomicComponent.class);
        wrapper = control.createMock(InstanceWrapper.class);
        scopeContainer = new CompositeScopeContainer(EasyMock.createNiceMock(ScopeContainerMonitor.class));
        scopeContainer.start();
    }
}
