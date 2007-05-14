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
package org.fabric3.fabric.wire.jdk;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Collections;

import junit.framework.TestCase;

import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.model.type.Scope;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

import org.easymock.EasyMock;
import org.osoa.sca.ServiceReference;

/**
 * @version $Rev$ $Date$
 */
public class JDKProxyServiceTestCase extends TestCase {
    private JDKProxyService proxyService;
    private Wire wire;

    public void testCastProxyToServiceReference() {
        Map<Method, InvocationChain> mapping = Collections.emptyMap();
        JDKInvocationHandler<Foo> handler = new JDKInvocationHandler<Foo>(Foo.class, wire, false, mapping, null);
        Foo proxy = handler.getService();
        ServiceReference<Foo> ref = proxyService.cast(proxy);
        assertSame(handler, ref);
    }

    protected void setUp() throws Exception {
        super.setUp();
        ScopeRegistry scopeRegistry = EasyMock.createMock(ScopeRegistry.class);
        EasyMock.expect(scopeRegistry.getScopeContainer(Scope.CONVERSATION)).andStubReturn(null);
        EasyMock.replay(scopeRegistry);
        wire = EasyMock.createMock(Wire.class);
        EasyMock.expect(wire.getCallbackInvocationChains()).andStubReturn(Collections.emptyMap());
        EasyMock.replay(wire);
        proxyService = new JDKProxyService(scopeRegistry);
    }

    public interface Foo {
    }
}
