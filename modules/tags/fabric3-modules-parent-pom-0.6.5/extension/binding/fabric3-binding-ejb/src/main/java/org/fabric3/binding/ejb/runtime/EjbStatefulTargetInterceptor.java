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

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.scdl.Signature;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.InstanceDestructionException;
import org.fabric3.spi.component.InstanceLifecycleException;
import org.fabric3.spi.invocation.Message;
import org.fabric3.spi.invocation.MessageImpl;


/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbStatefulTargetInterceptor extends BaseEjbTargetInterceptor {

    private final ScopeContainer scopeContainer;
    private final EjbStatefulComponent statefulComponent;
    private final boolean isPotentialCreateMethod;

    /**
     * Initializes the reference URL.
     */
    public EjbStatefulTargetInterceptor(Signature signature, EjbResolver resolver,
                                        ScopeContainer scopeContainer,
                                        EjbStatefulComponent statefulComponent) {
        super(signature, resolver);
        this.scopeContainer = scopeContainer;
        this.statefulComponent = statefulComponent;
        isPotentialCreateMethod = signature.getName().startsWith("create");
    }

    /**
     * @see org.fabric3.spi.wire.Interceptor#invoke(org.fabric3.spi.invocation.Message)
     */
    public Message invoke(Message message) {
        EjbStatefulInstanceWrapper wrap = null;
        Object sfsb = null;
        boolean isHome = false;

        try {
            wrap = (EjbStatefulInstanceWrapper)scopeContainer.getWrapper(statefulComponent, message.getWorkContext());
            sfsb = wrap.getInstance();

        } catch(InstanceLifecycleException tre) {
            throw new ServiceRuntimeException(tre);
        }

        if(sfsb == null) {
            sfsb = resolver.resolveStatefulEjb();

            isHome = sfsb instanceof EJBHome || sfsb instanceof EJBLocalHome;

            if(isHome && !isPotentialCreateMethod) {
                Message result = new MessageImpl();
                result.setBodyWithFault(new IllegalStateException(
                        "The target EJB resolved to an EJB 2.x home interface so the first invocation "+
                                "must be a create method"));
                return result;
            }
        }

        Message result = invoke(message, sfsb);

        if(isHome) {
            if(!message.isFault()) sfsb = result.getBody();
            
            // Don't return the EJBObject, the user needs to continue to invoke the same SCA reference
            result.setBody(null);
        }

        if(wrap.getInstance() == null) wrap.setInstance(sfsb);

        try {
            scopeContainer.returnWrapper(statefulComponent, message.getWorkContext(), wrap);
        } catch(InstanceDestructionException tde) {
            throw new ServiceRuntimeException(tde);
        }

        return result;
    }


}