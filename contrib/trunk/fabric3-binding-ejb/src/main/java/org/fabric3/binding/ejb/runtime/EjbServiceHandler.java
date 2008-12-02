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
package org.fabric3.binding.ejb.runtime;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.invocation.MessageImpl;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.model.physical.PhysicalOperationDefinition;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbServiceHandler implements InvocationHandler {

    /**
     * Map of op names to operation definitions.
     */
    private final Map<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops;


    public EjbServiceHandler(Map<Signature, Map.Entry<PhysicalOperationDefinition, InvocationChain>> ops) {
        this.ops = ops;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //TODO: I need to rethink this
        if (method.getName().equals("equals")
                && method.getParameterTypes().length == 1
                && (method.getParameterTypes()[0]).equals(Object.class)) {
            if (!Proxy.isProxyClass(args[0].getClass())) {
                return false;
            }
            InvocationHandler h = Proxy.getInvocationHandler(args[0]);
            return this.equals(h);
        }

        Signature signature = new Signature(method);
        Interceptor head = ops.get(signature).getValue().getHeadInterceptor();

        Message input = new MessageImpl(args, false, new WorkContext());

        Message output = head.invoke(input);
        if (output.isFault()) {
            throw (Throwable) output.getBody();
        } else {
            return output.getBody();
        }
    }
}
