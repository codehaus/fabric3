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

import org.fabric3.spi.invocation.WorkContextTunnel;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.InvocationChain;

/**
 * Responsible for dispatching to a callback service from a component implementation instance that is not composite scope. Since only one client can
 * invoke the instance this proxy is injected on at a time, there can only be one callback target, even if the proxy is injected on an instance
 * variable. Consequently, the proxy does not need to map the callback target based on the forward request.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class StatefulCallbackInvocationHandler<T> extends AbstractCallbackInvocationHandler<T> {
    private Map<Method, InvocationChain> chains;
    private ScopeContainer scopeContainer;

    /**
     * Constructor.
     *
     * @param interfaze the callback service interface implemented by the proxy
     * @param chains    the invocation chain mappings for the callback wire
     */
    public StatefulCallbackInvocationHandler(Class<T> interfaze, Map<Method, InvocationChain> chains) {
        super(interfaze);
        this.chains = chains;
    }

    /**
     * Constructor.
     *
     * @param interfaze      the callback service interface implemented by the proxy
     * @param scopeContainer the conversational scope container
     * @param chains         the invocation chain mappings for the callback wire
     */
    public StatefulCallbackInvocationHandler(Class<T> interfaze, ScopeContainer<?> scopeContainer, Map<Method, InvocationChain> chains) {
        super(interfaze);
        this.scopeContainer = scopeContainer;
        this.chains = chains;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        WorkContext workContext = WorkContextTunnel.getThreadWorkContext();
        // find the invocation chain for the invoked operation
        InvocationChain chain = chains.get(method);
        if (chain == null) {
            return handleProxyMethod(method);
        }
        try {
            return super.invoke(chain, args, workContext);
        } finally {
            if (chain.getPhysicalOperation().isEndsConversation()) {
                scopeContainer.stopContext(workContext);
            }
        }
    }

}