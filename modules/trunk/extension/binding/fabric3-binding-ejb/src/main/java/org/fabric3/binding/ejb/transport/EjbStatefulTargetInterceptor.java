/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
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
package org.fabric3.binding.ejb.transport;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;

import org.osoa.sca.ServiceRuntimeException;

import org.fabric3.binding.ejb.wire.EjbResolver;
import org.fabric3.scdl.Signature;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.TargetDestructionException;
import org.fabric3.spi.component.TargetResolutionException;
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

        } catch(TargetResolutionException tre) {
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
        } catch(TargetDestructionException tde) {
            throw new ServiceRuntimeException(tde);
        }

        return result;
    }


}