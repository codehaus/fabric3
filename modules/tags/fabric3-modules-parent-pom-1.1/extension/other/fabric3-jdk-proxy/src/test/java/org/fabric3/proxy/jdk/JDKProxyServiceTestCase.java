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
package org.fabric3.proxy.jdk;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;
import org.oasisopen.sca.ServiceReference;

import org.fabric3.spi.wire.InvocationChain;

/**
 * @version $Rev: 2883 $ $Date: 2008-02-24 11:05:35 -0800 (Sun, 24 Feb 2008) $
 */
public class JDKProxyServiceTestCase extends TestCase {
    private JDKProxyService proxyService;

    public void testCastProxyToServiceReference() {
        Map<Method, InvocationChain> mapping = Collections.emptyMap();
        JDKInvocationHandler<Foo> handler = new JDKInvocationHandler<Foo>(Foo.class, null, mapping);
        Foo proxy = handler.getService();
        ServiceReference<Foo> ref = proxyService.cast(proxy);
        assertSame(handler, ref);
    }

    protected void setUp() throws Exception {
        super.setUp();
        proxyService = new JDKProxyService();
    }

    public interface Foo {
    }
}
