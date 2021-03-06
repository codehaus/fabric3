/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
*
* Fabric3 is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as
* published by the Free Software Foundation, either version 3 of
* the License, or (at your option) any later version, with the
* following exception:
*
* Linking this software statically or dynamically with other
* modules is making a combined work based on this software.
* Thus, the terms and conditions of the GNU General Public
* License cover the whole combination.
*
* As a special exception, the copyright holders of this software
* give you permission to link this software with independent
* modules to produce an executable, regardless of the license
* terms of these independent modules, and to copy and distribute
* the resulting executable under terms of your choice, provided
* that you also meet, for each linked independent module, the
* terms and conditions of the license of that module. An
* independent module is a module which is not derived from or
* based on this software. If you modify this software, you may
* extend this exception to your version of the software, but
* you are not obligated to do so. If you do not wish to do so,
* delete this exception statement from your version.
*
* Fabric3 is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty
* of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU General Public License for more details.
*
* You should have received a copy of the
* GNU General Public License along with Fabric3.
* If not, see <http://www.gnu.org/licenses/>.
*/
package org.fabric3.implementation.proxy.jdk;

import java.lang.reflect.Method;
import java.util.Map;

import org.fabric3.model.type.component.Scope;
import org.fabric3.implementation.pojo.builder.ProxyService;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.CallFrame;
import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Returns a proxy instance for a callback wire.
 *
 * @version $Rev$ $Date$
 */
public class CallbackWireObjectFactory<T> implements ObjectFactory<T> {
    private ScopeContainer container;
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
                                     ScopeContainer container,
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
            CallFrame frame = WorkContextTunnel.getThreadWorkContext().peekCallFrame();
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
