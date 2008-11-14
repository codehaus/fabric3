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
package org.fabric3.proxy.jdk;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.pojo.PojoWorkContextTunnel;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.services.proxy.ProxyService;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Returns a proxy instance for a callback wire.
 *
 * @version $Rev: 3150 $ $Date: 2008-03-21 14:12:51 -0700 (Fri, 21 Mar 2008) $
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private ScopeContainer<?> container;
    private ProxyService proxyService;
    private Map<String, Map<Method, InvocationChain>> mappings;
    private Class<T> interfaze;

    /**
     * Constructor.
     *
     * @param interfaze    the proxy interface
     * @param container    the scope container of the component implementation the proxy will be injected on
     * @param proxyService the service for creating proxies
     * @param mappings     the callback URI to invocation chain mappings
     */
    public CallbackWireObjectFactory(Class<T> interfaze,
                                     ScopeContainer<?> container,
                                     ProxyService proxyService,
                                     Map<String, Map<Method, InvocationChain>> mappings) {
        this.interfaze = interfaze;
        this.container = container;
        this.proxyService = proxyService;
        this.mappings = mappings;
    }

    public T getInstance() throws ObjectCreationException {
        if (Scope.COMPOSITE.equals(container.getScope())) {
            return interfaze.cast(proxyService.createCallbackProxy(interfaze, mappings));
        } else {
            CallFrame frame = PojoWorkContextTunnel.getThreadWorkContext().peekCallFrame();
            String callbackUri = frame.getCallbackUri();
            assert callbackUri != null;
            Map<Method, InvocationChain> mapping = mappings.get(callbackUri);
            assert mapping != null;
            return interfaze.cast(proxyService.createStatefullCallbackProxy(interfaze, mapping, container));
        }
    }

    public Class<T> getInterfaze() {
        return interfaze;
    }

    public void updateMappings(String callbackUri, Map<Method, InvocationChain> chains) {
        mappings.put(callbackUri, chains);
    }

}
