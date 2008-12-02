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

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public abstract class BaseEjbTargetInterceptor implements Interceptor {

    protected final EjbResolver resolver;
    protected final Signature signature;
    private Method method = null;

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    
    public BaseEjbTargetInterceptor(Signature signature, EjbResolver resolver) {
        this.signature = signature;
        this.resolver = resolver;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#getNext()
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#setNext(org.fabric3.spi.wire.Interceptor)
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

    public abstract Message invoke(Message message);

    protected Message invoke(Message message, Object ejb) {
        Object[] parameters = (Object[]) message.getBody();

        if(method == null) {

            try {
                method = signature.getMethod(ejb.getClass());
            } catch(ClassNotFoundException cnfe) {
                throw new ServiceRuntimeException(cnfe);
            } catch(NoSuchMethodException nsme) {
                //TODO Give better error message
                throw new ServiceRuntimeException("The method "+signature+
                        " did not match any methods on the interface of the Target");
            }

        }


        Message result = new MessageImpl();
        try {
            result.setBody(method.invoke(ejb, parameters));
        } catch(InvocationTargetException ite) {
           result.setBodyWithFault(ite.getCause());
        } catch(Exception e) {
            throw new ServiceRuntimeException(e);
        }

        return result;
    }
    
}
