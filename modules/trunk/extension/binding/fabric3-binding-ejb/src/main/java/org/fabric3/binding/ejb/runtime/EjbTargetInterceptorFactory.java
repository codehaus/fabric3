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

import java.net.URI;

import javax.xml.namespace.QName;

import org.fabric3.binding.ejb.scdl.EjbBindingDefinition;
import org.fabric3.scdl.Signature;
import org.fabric3.scdl.Scope;
import org.fabric3.spi.component.ScopeContainer;
import org.fabric3.spi.component.ScopeRegistry;
import org.fabric3.spi.wire.Interceptor;

/**
 * @version $Revision Date: Oct 29, 2007 Time: 6:29:57 PM
 */
public class EjbTargetInterceptorFactory {

    private EjbBindingDefinition bd;
    private EjbResolver resolver;

    private ScopeContainer scopeContainer;
    private EjbStatefulComponent statefulComponent;


    public EjbTargetInterceptorFactory(EjbBindingDefinition bd, EjbResolver resolver, ScopeRegistry scopeRegistry, QName id) {
        this.bd = bd;
        this.resolver = resolver;

        if(!bd.isStateless()) {
            //TODO: do I need to start the StatefulComponent?
            statefulComponent = new EjbStatefulComponent(id);
            scopeContainer = scopeRegistry.getScopeContainer(Scope.CONVERSATION);
            scopeContainer.register(statefulComponent);
        }
    }

    public Interceptor getEjbTargetInterceptor(Signature signature) {
        if(bd.isStateless()) {
            return new EjbStatelessTargetInterceptor(signature, resolver);
        } else {
            return new EjbStatefulTargetInterceptor(signature, resolver, scopeContainer, statefulComponent);
        }

    }

}
