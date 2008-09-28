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
package org.fabric3.spring;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.ObjectCreationException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;

/**
 * @version $Revision$ $Date$
 */
public class SpringTargetInterceptor implements Interceptor {

    private Method method = null;
    private Object bean = null;
    
    Signature signature;
    SpringComponent<?> springComponent;

    /**
     * Next interceptor in the chain.
     */
    private Interceptor next;

    public SpringTargetInterceptor(Signature signature, SpringComponent<?> springComponent) {
        this.signature = signature;
        this.springComponent = springComponent;
    }
    
    public Message invoke(Message message) {
        Object bean = null;
        try {
            bean = springComponent.createObjectFactory().getInstance();
        } catch (ObjectCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Message result = invoke(message, bean);

        return result;
    }

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

}
