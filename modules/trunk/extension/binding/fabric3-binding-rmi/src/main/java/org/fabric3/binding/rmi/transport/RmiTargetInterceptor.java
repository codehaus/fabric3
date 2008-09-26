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
package org.fabric3.binding.rmi.transport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.rmi.wire.RmiReferenceFactory;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

public final class RmiTargetInterceptor implements Interceptor {

    private final RmiReferenceFactory factory;
    private final Method method;
    private Interceptor next;

    public RmiTargetInterceptor(Method method, RmiReferenceFactory factory) {
        this.method = method;
        this.factory = factory;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message message) {
        Object object = factory.getReference();
        Object[] parameters = (Object[]) message.getBody();
        Message result = new MessageImpl();
        try {
            result.setBody(Proxy.getInvocationHandler(object).invoke(object, method, parameters));
//            result.setBody(method.invoke(object, parameters));
        } catch (InvocationTargetException ite) {
            result.setBodyWithFault(ite.getCause());
        } catch (Throwable e) {
            if (e instanceof Error) {
                // re-throw an error since it should not be caught
                throw (Error) e;
            }
            throw new ServiceRuntimeException(e);
        }
        return result;
    }

}
